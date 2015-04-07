# nuxeo-platform-indexing-gateway

## Overview

This nuxeo platform plugin provides a simple WebService interface that can be used to :

 - browse the repository data

 - know what has changed in the repository in a given time range

 - read all data of a document (attributes, ACLs, Blobs ...)

This WS API is typically used for external indexers that needs to crawl Nuxeo repository and be able to know what has changed between 2 indexing.

## Recommendation and Warnings

### SOAP API

The SOAP API try to be compliant with most SOAP dialect and WS stacks.
This generates some limitations.

#### Authentication

Authentication is managed at Application level.

This means that the typical call flow will be :

 - connect(Login/Password) (return token)
 - doSomething(token, ...)
 - doSomething(token, ...)
 - doSomething(token, ...)
 - disconnect(token)

The client is responsible for calling disconnect at some point otherwise a leak will appear on the Nuxeo side.

#### Blobs management

SOAP badly handles Blob content and MTOM as caused a lot of incompatibilities.
So the current WebService uses a very simple approach :

 - use JAXB byte[] serialization in base64 : that is clearly not a good idea for performances and memory management

 - send a REST URL that can be used by the client to download the Blobs outside of the SOAP envelop

The choice of the format is left to the client via a boolean flag (useDownloadUrl).

#### Thread Safety

The token provided upon connection is supposed to be used by only one client thread at a time.
If for some reason, the client can not enforce that, a server side synchronization can be activated (use nuxeo.indexing.gateway.forceSync property).

### Security Mapping

Nuxeo will export all data about the Documents :

 - attributes (simple and complex)
 - Blobs
 - life-cycle
 - security descriptor

The tricky part is usually to handle security descriptor so that they can be understood by the client.

Since usually the client only need a *read_acl*, the WebService can provide that and also allows to plug a server side custom logic to define how the ACL linearization must be done.

For this the WebService can be contributed with a custom implementation of IndexingAdapter via an extension point.

## QA results

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=addons_nuxeo-platform-indexing-gateway-master)](https://qa.nuxeo.org/jenkins/job/addons_nuxeo-platform-indexing-gateway-master/)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.



