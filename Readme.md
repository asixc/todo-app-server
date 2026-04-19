## Pasos para el script
1. Compilación nativa genera una imagen de docker.
`./mvnw spring-boot:build-image -Pnative -DskipTests`

2. Guardamos la imagen generada en un archivo tar.
`docker save -o todo-api-app-native_0.0.4.tar todo-app:0.0.4-SNAPSHOT`

3. Copiamos el archivo tar a la máquina destino. (El ejemplo indica dos artefactos, front y back)
`rsync -avz -e 'ssh -p 41986' todo-api-app-native_0.0.3.tar todo-front-app-native_0.0.1.tar jotxee@192.168.1.206:~ `

4. Cargamos la imagen en la máquina destino.
`docker load -i todo-api-app-native_0.0.4.tar`

5. Ir a la ruta del archivo docker-compose.yml.
`cd /mnt/disco/docker/deployments/`

6. Hacerse root
`sudo su`

7. Modificar el archivo docker-compose.yml con la nueva imagen.
8. Tiene que ser parecido a esto:  `sed -i 's/image: " + projectName + ":[^ ]*/image: " + projectName + ":" + version + "/' docker-compose.yml"`
6. Levantamos el contenedor con la nueva imagen.
`docker-compose up -d`
