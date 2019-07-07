David Beck
CSC536
Final Project: MapReduce implementing Akka/Kubernetes cluster bootstraping & containerized with Docker


I leveraged most of the code base in my program from the mapreduce2 example provided in class in addition to the akka-sample-cluster-scala and akka-sample-cluster-kubernetes-java examples. This program is a very straight forward application of configuring an implementation to execute the concepts of bootstraping and service discovery through akka-discovery and kubernetes which incorporates etcd.

To run the project:

    1. Install and run docker and minikube. I would highly recommend to allocate enough memory to both docker (8 GB) and minikube (4 GB). I had persistent issues running on anything less.

    2. Run minikube: “minikube start”

    3. Configure the shell to target the minikube cluster: “eval $(minikube docker-env)”

    4. Within the project directory, go into each subfolder (“/FinalProject_DavidBeck/MapServer”,“/FinalProject_DavidBeck/Client”, and “/FinalProject_DavidBeck/ReduceServer”) and use command "sbt docker:publish". This action utilizes sbt-native-packager to generate a dockerfile, docker image and push the docker image to the docker server. Alternatively, you may use command “sbt docker:publishLocal” to publish the file locally.
	   Note: This step is not necessary because I have already pushed the images to docker. Though you can use these commands to check the build.sbt is executable. 

	5. This project uses .yml files to run the containers, you may need to pull images from my docker account. In the build.sbt files, I input my docker username which requires my credentials to push the images to my account. Instead, you may pull these images from my docker with these commands:

		“docker pull dbeck6/mapserver:v1.0.0”

		“docker pull dbeck6/reduceserver:v1.0.0”

		“docker pull dbeck6/client:v1.0.0”

    6. Go to kubernetes folder (“/FinalProject_DavidBeck/kubernetes”) and use the following commands IN THIS ORDER to create the containers with the .yml files to create the containers:

		“kubectl create –f akka-cluster-map.yml”

		“kubectl create –f akka-cluster-reduce.yml”
		
		Wait about 15 seconds then:

		“kubectl create –f akka-cluster-client.yml”

    7. You can use “kubectl get pods” to list the current pods and “kubectl logs {pod ID}” to see the logs of each cluster. The logs will show the clusters connecting and the reduce output.

	8. To delete the pods in the most effective way, use the following commands:
	
		“kubectl delete –f akka-cluster-client.yml”

		“kubectl delete –f akka-cluster-reduce.yml”

		“kubectl delete –f akka-cluster-map.yml”	
	