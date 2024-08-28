
#!/bin/bash
image=devopsaes/scmbe:0.0.19
docker build -t $image --no-cache .
echo  "image $image is built"
docker push $image
echo "image $image pushed"