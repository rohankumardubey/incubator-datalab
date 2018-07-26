/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/
/* tslint:disable:no-empty */

import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { ConfirmationDialogType } from './confirmation-dialog-type.enum';
import { UserResourceService, HealthStatusService, ManageEnvironmentsService } from '../../../core/services';

export class ConfirmationDialogModel {
  private title: string;
  private notebook: any;
  private confirmAction: Function;
  private manageAction: Function;
  private userResourceService: UserResourceService;
  private healthStatusService: HealthStatusService;
  private manageEnvironmentsService: ManageEnvironmentsService;

  static getDefault(): ConfirmationDialogModel {
    return new
      ConfirmationDialogModel(
      ConfirmationDialogType.StopExploratory, { name: '', resources: [] }, () => { }, () => { }, false, null, null, null);
  }

  constructor(
    confirmationType: ConfirmationDialogType,
    notebook: any,
    fnProcessResults: any,
    fnProcessErrors: any,
    manageAction,
    userResourceService: UserResourceService,
    healthStatusService: HealthStatusService,
    manageEnvironmentsService: ManageEnvironmentsService
  ) {
    this.userResourceService = userResourceService;
    this.healthStatusService = healthStatusService;
    this.manageEnvironmentsService = manageEnvironmentsService;
    this.manageAction = manageAction;
    this.setup(confirmationType, notebook, fnProcessResults, fnProcessErrors);
  }

  public isAliveResources(resources): boolean {
    if (resources) {
      for (let i = 0; i < resources.length; i++) {
        if (resources[i].status.toLowerCase() != 'failed'
          && resources[i].status.toLowerCase() != 'terminated'
          && resources[i].status.toLowerCase() != 'terminating'
          && resources[i].status.toLowerCase() != 'stopped')
            return true;
      }
    }

    return false;
  }


  private stopExploratory(): Observable<{} | Response> {
    return this.manageAction 
      ? this.manageEnvironmentsService.environmentManagement(this.notebook.user, 'stop', this.notebook.name)
      : this.userResourceService.suspendExploratoryEnvironment(this.notebook, 'stop');
  }

  private terminateExploratory(): Observable<{} | Response> {
    return this.manageAction 
      ? this.manageEnvironmentsService.environmentManagement(this.notebook.user, 'terminate', this.notebook.name)
      : this.userResourceService.suspendExploratoryEnvironment(this.notebook, 'terminate');
  }

  private stopEdgeNode(): Observable<{} | Response> {
    return this.manageAction 
      ? this.manageEnvironmentsService.environmentManagement(this.notebook.user, 'stop', 'edge')
      : this.healthStatusService.suspendEdgeNode();
  }

  private setup(confirmationType: ConfirmationDialogType, notebook: any, fnProcessResults: any, fnProcessErrors: any): void {
    const defaultStopMessage = 'Notebook server will be stopped.';
    const containRunningResourcesStopMessage = 'Notebook server will be stopped and all computational resources will be stopped/terminated.';

    const defaultTerminateMessage = 'Notebook server will be terminated.';
    const containRunningResourcesTerminateMessage = 'Notebook server and all computational resources will be terminated.';

    const edgeNodeStopMessage = 'Edge node will be stopped. You will need to start it later to proceed working with DLAB.';

    switch (confirmationType) {
      case ConfirmationDialogType.StopExploratory: {
        this.title = this.isAliveResources(notebook.resources) ? containRunningResourcesStopMessage : defaultStopMessage;
        this.notebook = notebook;
        this.confirmAction = () => this.stopExploratory()
          .subscribe((response: Response) => fnProcessResults(response),
          (response: Response) => fnProcessErrors(response));
      }
        break;
      case ConfirmationDialogType.TerminateExploratory: {
        this.title = this.isAliveResources(notebook.resources) ? containRunningResourcesTerminateMessage : defaultTerminateMessage;
        this.notebook = notebook;
        this.confirmAction = () => this.terminateExploratory()
          .subscribe((response: Response) => fnProcessResults(response),
          (response: Response) => fnProcessErrors(response));
      }
        break;
      case ConfirmationDialogType.StopEdgeNode: {
        this.title = edgeNodeStopMessage;
        this.notebook = notebook;
        this.confirmAction = () => this.stopEdgeNode()
          .subscribe((response: Response) => fnProcessResults(response),
          (response: Response) => fnProcessErrors(response));
      }
        break;
      default: {
        this.title = this.isAliveResources(notebook.resources) ? containRunningResourcesTerminateMessage : defaultTerminateMessage;
        this.notebook = notebook;
        this.confirmAction = () => this.stopExploratory()
          .subscribe((response: Response) => fnProcessResults(response),
          (response: Response) => fnProcessErrors(response));
      }
        break;
    }
  }
}
