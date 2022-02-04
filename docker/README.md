docker run --name test -e ROOT_PWD=root --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH  ham.base


docker run --name test -e DNS_HIJACK_SERVER=8.8.8.8 -e ROOT_PWD=root --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH  ham.client