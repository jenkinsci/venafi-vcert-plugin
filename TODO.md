VCertConnector(
    type

    TPP:
        baseUrl
        username
        password

    Cloud:
        apiKey
)

venafiRequestCertificate(
    connectorName

    commonName
    organization
    organizationUnit
    country
    locality
    province

    dnsNames
    ipAddresses
    emailAddresses
    keyType

    privKeyOutput
    certOutput
    certChainOutput
)
