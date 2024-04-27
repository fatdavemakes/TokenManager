DATE=$(shell date -I)
VERSIONNUMBER=$(shell git describe --dirty=-dirty-$(DATE))
PLUGINBUILDNAME?=TokenManager
COMMANDPREFIX ?=
COMMANDSUFFIX ?=
PERMISSIONPREFIX ?=
PERMISSIONSUFFIX ?=
all:
	echo Date: $(DATE)
	echo Build Name: $(PLUGINBUILDNAME)
	echo Version Number: $(VERSIONNUMBER)
	echo Command Prefix: $(COMMANDPREFIX)
	echo Command Suffix: $(COMMANDSUFFIX)
	echo Permission Prefix: $(PERMISSIONPREFIX)
	echo Permission Suffix: $(PERMISSIONSUFFIX)	
	cp template_plugin.yml ./src/main/resources/plugin.yml
	cp template_settings.gradle ./settings.gradle
	sed -i 's/PLUGINBUILDNAME/$(PLUGINBUILDNAME)/g' ./src/main/resources/plugin.yml
	sed -i 's/COMMANDPREFIX/$(COMMANDPREFIX)/g' ./src/main/resources/plugin.yml
	sed -i 's/COMMANDSUFFIX/$(COMMANDSUFFIX)/g' ./src/main/resources/plugin.yml
	sed -i 's/PERMISSIONPREFIX/$(PERMISSIONPREFIX)/g' ./src/main/resources/plugin.yml
	sed -i 's/PERMISSIONSUFFIX/$(PERMISSIONSUFFIX)/g' ./src/main/resources/plugin.yml
	sed -i 's/PLUGINBUILDNAME/$(PLUGINBUILDNAME)/g' ./settings.gradle
	sh -c "./gradlew -i -Pversion=$(VERSIONNUMBER) build"


    
    
