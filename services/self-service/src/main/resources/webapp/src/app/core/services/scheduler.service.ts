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

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApplicationServiceFacade } from '.';
import { ErrorUtils } from '../util/';

@Injectable()
export class SchedulerService {
  constructor(private applicationServiceFacade: ApplicationServiceFacade) {}

  public getExploratorySchedule(notebook, resource?): Observable<{}> {
    const param = resource ? `/${notebook}/${resource}` : `/${notebook}`;
    return this.applicationServiceFacade
      .buildGetExploratorySchedule(param)
      .map(response => response.json())
      .catch(ErrorUtils.handleServiceError);
  }

  public setExploratorySchedule(notebook, data, resource?): Observable<{}> {
    const param = resource ? `/${notebook}/${resource}` : `/${notebook}`;
    return this.applicationServiceFacade
      .buildSetExploratorySchedule(param, data)
      .map(response => response)
      .catch(ErrorUtils.handleServiceError);
  }

  public resetScheduleSettings(notebook, resource?): Observable<{}> {
    const url = resource ? `/${notebook}/${resource}` : `/${notebook}`;
    return this.applicationServiceFacade
      .buildResetScheduleSettings(JSON.stringify(url))
      .map(response => response)
      .catch(ErrorUtils.handleServiceError);
  }

  public getActiveSchcedulersData(offset: number): Observable<{}> {
    const param = `/active?minuteOffset=${offset}`;
    return this.applicationServiceFacade
      .BuildGetActiveSchcedulersData(param)
      .map(response => response.json())
      .catch(ErrorUtils.handleServiceError);
  }
}
