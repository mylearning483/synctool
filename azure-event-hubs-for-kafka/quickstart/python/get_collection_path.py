#!/usr/bin/env python
#
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright 2016 Confluent Inc.
# Licensed under the MIT License.
# Licensed under the Apache License, Version 2.0
#
# Original Confluent sample modified for use with Azure Event Hubs for Apache Kafka Ecosystems

import sys
import getopt
import json
import logging
from pprint import pformat

import requests

PURVIEW_HOST = 'MyLearning483.purview.azure.com'
ROOT_COLLECTION = "usb_root"       # Root collection for US Bank datasets
ROOT_COLLECTION = "MyLearning483"  # TODO: use until we reorg collections under 'usb_root'


def stats_cb(stats_json_str):
    stats_json = json.loads(stats_json_str)
    print('\nKAFKA Stats: {}\n'.format(pformat(stats_json)))


def print_usage_and_exit(program_name):
    sys.stderr.write(f'Usage:   {program_name} <collection_name>\n')
    sys.stderr.write(f'Example: {program_name} sqvhq0\n')
    sys.exit(1)


def get_collection_path(access_token, collection_name):
    url = f'https://{PURVIEW_HOST}/collections/{collection_name}?api-version=2019-11-01-preview'
    payload={}
    headers = { 
        'Authorization': f'Bearer {access_token}'
    }
    r = requests.request("GET", url, headers=headers, data=payload)
    r_json = json.loads(r.text)
    #print(f'Collection response={r_json}')
    friendly_name = r_json['friendlyName']
    if friendly_name == ROOT_COLLECTION:         # reached root?
        return ""

    parent_name = r_json ['parentCollection']['referenceName']
    parent_path = get_collection_path(access_token, parent_name)
    return f'{parent_path}/{friendly_name}'


if __name__ == '__main__':
    optlist, argv = getopt.getopt(sys.argv[1:], 'T:')
    if len(argv) != 1:
        print_usage_and_exit(sys.argv[0])

    collection_name = argv[0]

    # Create logger for consumer (logs will be emitted when poll() is called)
    logger = logging.getLogger('collection_path')
    logger.setLevel(logging.DEBUG)
    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter('%(asctime)-15s %(levelname)-8s %(message)s'))
    logger.addHandler(handler)

    # Get access_token

    url = "https://login.microsoftonline.com/bc1b9bc3-427e-4ff0-902e-161275439414/oauth2/token"

    payload={
        'client_id': 'a3bcd354-f5a2-4865-bbb3-88d694ae5018',
        'client_secret': 'fEI8Q~8V-B-QPLbHOD64eEzYpxSK_tA4.k9-mcO_',
        'resource': 'https://purview.azure.net',
        'scope': './default',
        'grant_type': 'client_credentials'
        }
    headers = {}
    files = []
    try:
        r = requests.request("POST", url, headers=headers, data=payload, files=files)
        r_json = json.loads(r.text)
        access_token = r_json['access_token']
        #print(f'access_token=\n{access_token}\n')
    except Exception as e:
        print(f'ERROR: failed to get access token\n {e}')
        raise SystemExit(e)

    # Recursively build collection path
    try:
        collection_path = get_collection_path(access_token, collection_name)
        print(f'Collection path for collection {collection_name} = {collection_path}')
    except Exception as e:
        print(f'ERROR: failed to build collection path\n {e}')
        raise SystemExit(e)
       




