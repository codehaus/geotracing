SVN
The structure of the GeoTracing SVN repository is as follows:

base       - core framework
  trunk    - main branch for core framework
  tags
  branches

app             - geotracing applications
  bliin         - bliin app
    trunk       - main branch for bliin
    tags
    branches
  geoskating    - geoskating app
    trunk       - main branch for geoskating
    tags
    branches
  otter
    .
    .
etc, i.e. each application has its own trunk/tags/branches subdirs

BUILDING
to build apps you have to build the base first

cd base/trunk
ant

Notes:
- builds may fail because of security.properties are missing
for obvious reasons these files ar not in SVN. You should provide 
a security.properties file based on the template file security.properties.template
found in the same dir as security.properties would reside.

