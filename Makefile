DATE=$(shell date -I)
VERSIONNUMBER=$(shell git describe --dirty=-dirty-$(DATE))
all:
	echo $(DATE)
	echo $(VERSIONNUMBER)
	sh -c "./gradlew -Pversion=$(VERSIONNUMBER) build"
    
    
