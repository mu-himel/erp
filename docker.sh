#!/bin/bash
image=devopsaes/scmbe:v1.3.23
docker build -t $image --no-cache .
echo  "image $image is built"
docker push $image
echo "image $image pushed"