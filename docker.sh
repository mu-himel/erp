#!/bin/bash
image=devopsaes/scmbe:1.1.0
docker build -t $image --no-cache .
echo  "image $image is built"
docker push $image
echo "image $image pushed"