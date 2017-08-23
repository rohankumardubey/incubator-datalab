#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************

import logging
import json
import sys
from dlab.fab import *
from dlab.meta_lib import *
from dlab.actions_lib import *
import os
import uuid
import argparse
import sys


def stop_notebook(instance_name, bucket_name, zone, ssh_user, key_path):
    print 'Terminating Dataproc cluster and cleaning Dataproc config from bucket'
    try:
        clusters_list = meta_lib.GCPMeta().get_dataproc_list(instance_name)
        if clusters_list:
            for cluster_name in clusters_list:
                cluster = meta_lib.GCPMeta().get_list_cluster_statuses([cluster_name])
                actions_lib.GCPActions().bucket_cleanup(bucket_name, os.environ['edge_user_name'], cluster_name)
                print 'The bucket {} has been cleaned successfully'.format(bucket_name)
                actions_lib.GCPActions().delete_dataproc_cluster(cluster_name, os.environ['gcp_region'])
                print 'The Dataproc cluster {} has been terminated successfully'.format(cluster_name)
                actions_lib.GCPActions().remove_kernels(instance_name, cluster_name, cluster[0]['version'], ssh_user, key_path)
        else:
            print "There are no Dataproc clusters to terminate."
    except:
       sys.exit(1)

    print "Stopping notebook"
    try:
        GCPActions().stop_instance(instance_name, zone)
    except Exception as err:
        append_result("Failed to stop notebook.", str(err))
        sys.exit(1)


if __name__ == "__main__":
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'],
                                               os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    print 'Generating infrastructure names and tags'
    notebook_config = dict()
    notebook_config['service_base_name'] = os.environ['conf_service_base_name']
    notebook_config['notebook_name'] = os.environ['notebook_instance_name']
    notebook_config['bucket_name'] = (notebook_config['service_base_name'] + '-ssn-bucket').lower().replace('_', '-')
    notebook_config['key_path'] = os.environ['conf_key_dir'] + os.environ['conf_key_name'] + '.pem'
    notebook_config['zone'] = os.environ['gcp_zone']

    logging.info('[STOP NOTEBOOK]')
    print '[STOP NOTEBOOK]'
    try:
        stop_notebook(notebook_config['notebook_name'], notebook_config['bucket_name'], notebook_config['zone'],
                      os.environ['conf_os_user'], notebook_config['key_path'])
    except Exception as err:
        append_result("Failed to stop notebook.", str(err))
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"notebook_name": notebook_config['notebook_name'],
                   "Action": "Stop notebook server"}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        print "Failed writing results."
        sys.exit(0)

