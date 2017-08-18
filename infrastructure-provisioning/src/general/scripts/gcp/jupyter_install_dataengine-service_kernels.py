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

import argparse
from fabric.api import *
import boto3
from dlab.meta_lib import *
from dlab.actions_lib import *
import os

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--dataproc_version', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--notebook_ip', type=str, default='')
parser.add_argument('--scala_version', type=str, default='')
parser.add_argument('--dataproc_excluded_spark_properties', type=str, default='')
parser.add_argument('--edge_user_name', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--edge_hostname', type=str, default='')
parser.add_argument('--proxy_port', type=str, default='')
parser.add_argument('--pip_mirror', type=str, default='')
parser.add_argument('--application', type=str, default='')
args = parser.parse_args()


def configure_notebook(args):
    templates_dir = '/root/templates/'
    files_dir = '/root/files/'
    scripts_dir = '/root/scripts/'
    put(templates_dir + 'pyspark_dataengine-service_template.json', '/tmp/pyspark_dataengine-service_template.json')
    put(templates_dir + 'r_dataengine-service_template.json', '/tmp/r_dataengine-service_template.json')
    put(templates_dir + 'toree_dataengine-service_template.json','/tmp/toree_dataengine-service_template.json')
    put(scripts_dir + '{}_create_configs.py'.format(args.application), '/tmp/create_configs.py')
    put(files_dir + 'toree_kernel.tar.gz', '/tmp/toree_kernel.tar.gz')
    put(templates_dir + 'toree_dataengine-service_templatev2.json', '/tmp/toree_dataengine-service_templatev2.json')
    put(templates_dir + 'run_template.sh', '/tmp/run_template.sh')
    sudo('\cp /tmp/create_configs.py /usr/local/bin/create_configs.py')
    sudo('chmod 755 /usr/local/bin/create_configs.py')
    sudo('mkdir -p /usr/lib/python2.7/dlab/')
    run('mkdir -p /tmp/dlab_libs/')
    local('scp -i {} /usr/lib/python2.7/dlab/* {}:/tmp/dlab_libs/'.format(args.keyfile, env.host_string))
    run('chmod a+x /tmp/dlab_libs/*')
    sudo('mv /tmp/dlab_libs/* /usr/lib/python2.7/dlab/')
    if exists('/usr/lib64'):
        sudo('ln -fs /usr/lib/python2.7/dlab /usr/lib64/python2.7/dlab')


if __name__ == "__main__":
    env.hosts = "{}".format(args.notebook_ip)
    env.user = args.os_user
    env.key_filename = "{}".format(args.keyfile)
    env.host_string = env.user + "@" + env.hosts
    configure_notebook(args)
    spark_version = actions_lib.GCPActions().get_cluster_app_version(args.bucket, args.edge_user_name, args.cluster_name, 'spark')
    hadoop_version = actions_lib.GCPActions().get_cluster_app_version(args.bucket, args.edge_user_name, args.cluster_name, 'hadoop')
    # sudo("/usr/bin/python /usr/local/bin/create_configs.py --bucket " + args.bucket + " --cluster_name "
    #      + args.cluster_name + " --dataproc_version " + args.dataproc_version + " --spark_version " + spark_version
    #      + " --hadoop_version " + hadoop_version + " --region " + args.region + " --excluded_lines '"
    #      + args.dataproc_excluded_spark_properties + "' --user_name " + args.edge_user_name + " --os_user " + args.os_user +
    #      " --pip_mirror " + args.pip_mirror + " --application " + args.application)
    sudo('echo "[global]" > /etc/pip.conf; echo "proxy = $(cat /etc/profile | grep proxy | head -n1 | cut -f2 -d=)" >> /etc/pip.conf')
    sudo('echo "use_proxy=yes" > ~/.wgetrc; echo "https_proxy=$(cat /etc/profile | grep proxy | head -n1 | cut -f2 -d=)" >> ~/.wgetrc')
    sudo('unset http_proxy https_proxy; export gcp_project_id="{0}"; export conf_resource="{1}"; /usr/bin/python /usr/local/bin/create_configs.py --bucket {2} --cluster_name {3} --dataproc_version {4} --spark_version {5} --hadoop_version {6} --region {7} --excluded_lines \'{8}\' --user_name {9} --os_user {10} --pip_mirror {11} --application {12}'
         .format(os.environ['gcp_project_id'], os.environ['conf_resource'], args.bucket, args.cluster_name, args.dataproc_version, spark_version, hadoop_version,
                 args.region, args.dataproc_excluded_spark_properties, args.edge_user_name, args.os_user, args.pip_mirror, args.application))
