# Venafi Machine Identity Protection plugin for Jenkins

This plugin integrates [Venafi Machine Identity Protection](https://support.venafi.com/hc/en-us/articles/217991528-Introducing-VCert-API-Abstraction-for-DevOps) with Jenkins-based CI/CD processes.

## Setup & usage overview

You must already have access to one or more Venafi VCert connectors. This could either be a Venafi Trust Protection Platform™ (TPP), or Venafi Cloud. Configure the connectors you have, and their credentials, in Manage Jenkins ➜ Configure System ➜ Venafi Machine Identity Protection.

Once the connectors are set up, you are ready to proceed with main usage: see [Build steps & pipeline functions](#build-steps-pipeline-functions).

## Build steps & pipeline functions

All operations listed here are compatible with both freestyle projects (Build steps) as well as pipeline projects (Pipeline functions).

### Venafi Machine Identity Protection: request certificate (`venafiVcertRequestCertificate`)

Requests a pair of certificate + private key. The output is to be written to the specified files, in PEM format.

#### Example pipeline usage

~~~groovy
venafiVcertRequestCertificate connectorName: 'Venafi Cloud',
    zoneConfigName: 'Certificates\\VCert',
    keyType: 'RSA',
    commonName: 'yourdomain.com',
    organization: 'orgname',
    organizationalUnit: 'orgunit',
    locality: 'Amsterdam',
    province: 'Noord-Holland',
    country: 'NL',
    privKeyOutput: 'priv.key',
    certOutput: 'cert.crt',
    certChainOutput: 'certchain.crt'
~~~

#### Pipeline paramters: general

Required:

 * `connectorName`: The Venafi VCert connector to use.
 * `keyType`: either 'RSA' or 'ECDSA'.
 * `zoneConfigName`: the name of the zone configuration to use.

Optional:

 * `dnsNames`: a list of DNS names, as part of the certificate's Alternative Subject Names.

    Syntax:

    ~~~groovy
    venafiVcertRequestCertificate ..., dnsNames: [[hostName: 'host1.com'], [hostName: 'host2.com']]
    ~~~

 * `ipAddresses`: a list of IP addresses, as part of the certificate's Alternative Subject Names.

    Syntax:

    ~~~groovy
    venafiVcertRequestCertificate ..., ipAddresses: [[address: '127.0.0.1'], [address: '127.0.0.2']]
    ~~~

 * `emailAddresses`: a list of email addresses, as part of the certificate's Alternative Subject Names.

    Syntax:

    ~~~groovy
    venafiVcertRequestCertificate ..., emailAddresses: [[address: 'a@a.com'], [address: 'b@b.com']]
    ~~~

#### Pipeline paramters: subject information

Required:

 * `commonName`: the certificate's common name.

Required or optional, depending on the VCert connector zone configuration:

 * `organization`
 * `organizationalUnit`
 * `locality`
 * `province`
 * `country`

#### Pipeline parameters: output

Required:

 * `privKeyOutput`: a path to which the private key should be written.
 * `certOutput`: a path to which the certificate should be written.
 * `certChainOutput`: a path to which the certificate chain should be written.
