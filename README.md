# Venafi Machine Identity Protection plugin for Jenkins

This plugin integrates [Venafi Machine Identity Protection](https://support.venafi.com/hc/en-us/articles/217991528-Introducing-VCert-API-Abstraction-for-DevOps) with Jenkins-based CI/CD processes.

## Setup & usage overview

You must already have access to either Venafi TLS Protect (part of the Venafi Trust Protection Platform™), or Venafi DevOpsACCELERATE (part of Venafi Cloud). Configure their credentials and other parameters, in Manage Jenkins ➜ Configure System ➜ Venafi Machine Identity Protection.

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

 * `connectorName`: The Venafi connector to use.
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

 * `expirationWindow`: number of hours before certificate expiry to request a new certificate. We'll check whether the certificate file referenced by `certOutput` already exists, and if so, we'll only proceed with requesting a new certificate if the file's expiry date is within `expirationWindow` hours. Learn more at [Renewing certificate only when expiration is near](#renewing-certificate-only-when-expiration-is-near).

#### Pipeline parameters: subject information

Required:

 * `commonName`: the certificate's common name.

Required or optional, depending on the connector's zone configuration:

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

## Renewing certificate only when expiration is near

Normally, `venafiVcertRequestCertificate` requests a certificate every time it's called. Sometimes you only want to request a certificate when it's about to expire. The `expirationWindow` parameter addresses this use case.

`expirationWindow` expects that you fetch the previously generated certificate, and store it under the same path as specified by `certOutput`. The `expirationWindow` feature will then check whether that file's expiry date is within `expirationWindow` hours. If so, or if there is no previously generated certificate, then it'll proceed with requesting a new certificate. Otherwise it does nothing, and logs this decision.

Here's an example of idiomatic usage. It requires the [Copy Artifacts plugin](https://plugins.jenkins.io/copyartifact/).

~~~groovy
// Fetch previous certificate.
try {
   copyArtifacts(projectName: currentBuild.projectName, selector: lastSuccessful(), filter: 'cert.crt')
} catch (e) {
   // Don't fail pipeline if there is no previous certificate.
   echo "Error fetching previous cert.crt: ${e}"
}

// Only request a new certificate if there is no previous certificate,
// or if the previous certificate expires within 48 hours.
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
    certChainOutput: 'certchain.crt',
    expirationWindow: 48

// Archive the certificate file as an artifact. This file could
// be a newly requested certificate, or it could be the previous certificate.
// The next pipeline build will attempt to fetch this file.
archiveArtifacts(artifacts: 'cert.crt')
~~~

Tip: when using declarative pipelines, make sure to put the example code in `script` block so that the `try...catch` construct works.
