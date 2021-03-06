/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.epam.datalab.backendapi.service.impl;

import com.epam.datalab.auth.UserInfo;
import com.epam.datalab.backendapi.annotation.Audit;
import com.epam.datalab.backendapi.annotation.Info;
import com.epam.datalab.backendapi.annotation.Project;
import com.epam.datalab.backendapi.annotation.ResourceName;
import com.epam.datalab.backendapi.annotation.User;
import com.epam.datalab.backendapi.dao.ExploratoryDAO;
import com.epam.datalab.backendapi.dao.ExploratoryLibDAO;
import com.epam.datalab.backendapi.dao.ImageExploratoryDAO;
import com.epam.datalab.backendapi.domain.EndpointDTO;
import com.epam.datalab.backendapi.domain.ProjectDTO;
import com.epam.datalab.backendapi.resources.dto.ImageInfoRecord;
import com.epam.datalab.backendapi.service.EndpointService;
import com.epam.datalab.backendapi.service.ImageExploratoryService;
import com.epam.datalab.backendapi.service.ProjectService;
import com.epam.datalab.backendapi.util.RequestBuilder;
import com.epam.datalab.constants.ServiceConsts;
import com.epam.datalab.dto.UserInstanceDTO;
import com.epam.datalab.dto.UserInstanceStatus;
import com.epam.datalab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.datalab.dto.exploratory.ImageStatus;
import com.epam.datalab.exceptions.ResourceAlreadyExistException;
import com.epam.datalab.exceptions.ResourceNotFoundException;
import com.epam.datalab.model.ResourceType;
import com.epam.datalab.model.exploratory.Image;
import com.epam.datalab.model.library.Library;
import com.epam.datalab.rest.client.RESTService;
import com.epam.datalab.rest.contracts.ExploratoryAPI;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.datalab.backendapi.domain.AuditActionEnum.CREATE;
import static com.epam.datalab.backendapi.domain.AuditResourceTypeEnum.IMAGE;

@Singleton
@Slf4j
public class ImageExploratoryServiceImpl implements ImageExploratoryService {
    private static final String IMAGE_EXISTS_MSG = "Image with name %s is already exist in project %s";
    private static final String IMAGE_NOT_FOUND_MSG = "Image with name %s was not found for user %s";

    @Inject
    private ExploratoryDAO exploratoryDAO;
    @Inject
    private ImageExploratoryDAO imageExploratoryDao;
    @Inject
    private ExploratoryLibDAO libDAO;
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;
    @Inject
    private RequestBuilder requestBuilder;
    @Inject
    private EndpointService endpointService;
    @Inject
    private ProjectService projectService;

    @Audit(action = CREATE, type = IMAGE)
    @Override
    public String createImage(@User UserInfo user, @Project String project, @ResourceName String exploratoryName, String imageName, String imageDescription, @Info String info) {
        ProjectDTO projectDTO = projectService.get(project);
        UserInstanceDTO userInstance = exploratoryDAO.fetchRunningExploratoryFields(user.getName(), project, exploratoryName);

        if (imageExploratoryDao.exist(imageName, userInstance.getProject())) {
            log.error(String.format(IMAGE_EXISTS_MSG, imageName, userInstance.getProject()));
            throw new ResourceAlreadyExistException(String.format(IMAGE_EXISTS_MSG, imageName, userInstance.getProject()));
        }
        final List<Library> libraries = libDAO.getLibraries(user.getName(), project, exploratoryName);

        imageExploratoryDao.save(Image.builder()
                .name(imageName)
                .description(imageDescription)
                .status(ImageStatus.CREATING)
                .user(user.getName())
                .libraries(fetchExploratoryLibs(libraries))
                .computationalLibraries(fetchComputationalLibs(libraries))
                .dockerImage(userInstance.getImageName())
                .exploratoryId(userInstance.getId())
                .project(userInstance.getProject())
                .endpoint(userInstance.getEndpoint())
                .build());

        exploratoryDAO.updateExploratoryStatus(new ExploratoryStatusDTO()
                .withUser(user.getName())
                .withProject(project)
                .withExploratoryName(exploratoryName)
                .withStatus(UserInstanceStatus.CREATING_IMAGE));

        EndpointDTO endpointDTO = endpointService.get(userInstance.getEndpoint());
        return provisioningService.post(endpointDTO.getUrl() + ExploratoryAPI.EXPLORATORY_IMAGE,
                user.getAccessToken(),
                requestBuilder.newExploratoryImageCreate(user, userInstance, imageName, endpointDTO, projectDTO), String.class);
    }

    @Override
    public void finishImageCreate(Image image, String exploratoryName, String newNotebookIp) {
        log.debug("Returning exploratory status with name {} to RUNNING for user {}",
                exploratoryName, image.getUser());
        exploratoryDAO.updateExploratoryStatus(new ExploratoryStatusDTO()
                .withUser(image.getUser())
                .withProject(image.getProject())
                .withExploratoryName(exploratoryName)
                .withStatus(UserInstanceStatus.RUNNING));
        imageExploratoryDao.updateImageFields(image);
        if (newNotebookIp != null) {
            log.debug("Changing exploratory ip with name {} for user {} to {}", exploratoryName, image.getUser(),
                    newNotebookIp);
            exploratoryDAO.updateExploratoryIp(image.getUser(), image.getProject(), newNotebookIp, exploratoryName);
        }

    }

    @Override
    public List<ImageInfoRecord> getNotFailedImages(String user, String dockerImage, String project, String endpoint) {
        return imageExploratoryDao.getImages(user, dockerImage, project, endpoint, ImageStatus.CREATED, ImageStatus.CREATING);
    }

    @Override
    public ImageInfoRecord getImage(String user, String name, String project, String endpoint) {
        return imageExploratoryDao.getImage(user, name, project, endpoint).orElseThrow(() ->
                new ResourceNotFoundException(String.format(IMAGE_NOT_FOUND_MSG, name, user)));
    }

    @Override
    public List<ImageInfoRecord> getImagesForProject(String project) {
        return imageExploratoryDao.getImagesForProject(project);
    }

    private Map<String, List<Library>> fetchComputationalLibs(List<Library> libraries) {
        return libraries.stream()
                .filter(resourceTypePredicate(ResourceType.COMPUTATIONAL))
                .collect(Collectors.toMap(Library::getResourceName, Lists::newArrayList, this::merge));
    }

    private List<Library> merge(List<Library> oldValue, List<Library> newValue) {
        oldValue.addAll(newValue);
        return oldValue;
    }

    private List<Library> fetchExploratoryLibs(List<Library> libraries) {
        return libraries.stream()
                .filter(resourceTypePredicate(ResourceType.EXPLORATORY))
                .collect(Collectors.toList());
    }

    private Predicate<Library> resourceTypePredicate(ResourceType resourceType) {
        return l -> resourceType == l.getType();
    }
}
