#!/bin/bash
image=devopsaes/scmbe:v1.5.32
docker build -t $image --no-cache -f /home/user/projects/erp-scm/Dockerfile /home/user/projects/erp-scm
echo  "image $image is built"
docker push $image
echo "image $image pushed"