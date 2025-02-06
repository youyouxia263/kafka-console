REPO?=image.cestc.cn/cmq/kafka-agent
TAG:=latest
ARCH=amd64
PWD=$(shell pwd)
#amd64/arm64

buildx: compile
	@echo "it will buildx multiarch docker image"
	docker buildx build --no-cache --platform 'linux/arm64,linux/amd64'  -t $(REPO):$(TAG) -f Dockerfile ./ --push

compile:
	@echo "it compile cmq-common project!"
	mvn clean package -T 1C -Dmaven.test.skip=true -Dmaven.compile.fork=true