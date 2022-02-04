docker build -t sampleapp .

docker run --name sampleappimpl --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH  --dns=127.0.0.1 sampleapp


docker run --name test -e ROOT_PWD=root --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH  ham.base