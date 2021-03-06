/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy  of the License at
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

export const PATTERNS = {
  namePattern: '[-_a-zA-Z0-9]*[_-]*[a-zA-Z0-9]+',
  projectName: '[a-zA-Z0-9]+',
  delimitersRegex: '[-_]?',
  url: '[a-zA-Z0-9.://%#&\\.@:%-_\+~#=]*\.[^\s]*[a-zA-Z0-9]/+',
  nodeCountPattern: '^[1-9]\\d*$',
  integerRegex: '^[0-9]*$',
  folderRegex: /^[a-zA-Z0-9!@$^&*()_+\-=\[\]{};':|,.<>~` ]*$/,
  libVersion: /^[a-zA-Z0-9_\-:/~.+]*$/,
  fullUrl: /^(http?|ftp|https):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+([.:])(\d{4}|com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*\/$/
};
