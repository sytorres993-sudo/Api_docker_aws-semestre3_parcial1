# Servidor Linux: Spring Boot + Nginx + RDS PostgreSQL

Esta configuración publica el frontend y la API a través de Nginx. La base de datos es RDS: este Compose no crea un PostgreSQL local. El backend se construye con el `Dockerfile` y `src/` de la raíz del proyecto.

```text
Gimnasio_sara/
├── Dockerfile
├── pom.xml
├── src/                         # backend en capas y frontend
└── deploy/server/
    ├── docker-compose.yml       # backend y nginx
    ├── .env.example             # plantilla sin credenciales reales
    ├── .env                     # se configura solo en el servidor
    ├── preparar-servidor.sh
    ├── certs/global-bundle.pem   # certificado público descargado por el script
    └── nginx/default.conf
```

## Subir el proyecto

Necesitas la IP o DNS de EC2, el usuario SSH y una clave autorizada para esa instancia. No se puede identificar la instancia a partir de una clave privada por sí sola.

Si recibiste el paquete `gimnasio-sara-servidor.tar.gz`, cópialo desde PowerShell (reemplaza usuario y servidor):

```powershell
scp -i .\claveGym.pem .\deploy\artifacts\gimnasio-sara-servidor.tar.gz ubuntu@IP_SERVIDOR:/tmp/gimnasio-sara-servidor.tar.gz
ssh -i .\claveGym.pem ubuntu@IP_SERVIDOR
```

En la sesión SSH, utiliza un directorio nuevo para no sobreescribir un despliegue existente:

```bash
mkdir -p ~/gimnasio-sara
cd ~/gimnasio-sara
tar -xzf /tmp/gimnasio-sara-servidor.tar.gz
cd deploy/server
sh preparar-servidor.sh
nano .env
```

Docker Engine y el complemento Docker Compose deben estar instalados. El usuario SSH debe poder utilizar Docker. Si la carpeta de destino contiene una aplicación existente, revisa su contenido antes de extraer el paquete.

## Configurar RDS

En `.env`, configura `DB_URL`, `DB_USER` y `DB_PASSWORD` con los datos reales. Mantén `sslmode=verify-full` y la ruta del certificado. `DB_URL` debe usar el endpoint DNS de RDS; el nombre de la base debe existir y el usuario necesita permisos para las migraciones Flyway. No utilices la contraseña del PostgreSQL de Docker como sustituto de la contraseña de RDS.

La instancia EC2 necesita conectividad de red hacia RDS. En el Security Group de RDS permite TCP 5432 desde el Security Group de EC2. En EC2 permite TCP 80 para el acceso al sitio y restringe SSH a tu IP. El backend no publica el puerto 8080 hacia el exterior.

## Lanzar

Desde `deploy/server`, después de completar `.env`:

```bash
docker compose config --quiet
docker compose up --build -d
docker compose ps
docker compose logs --tail 80 backend nginx
curl --fail http://127.0.0.1/actuator/health
```

Puedes esperar explícitamente a los chequeos de salud con `docker compose up --build -d --wait --wait-timeout 300`. La primera compilación descarga dependencias y ejecuta las pruebas.

Si ambos contenedores están saludables, abre `http://IP_SERVIDOR/` para el frontend. Las API serán `http://IP_SERVIDOR/api/ejercicios` y `http://IP_SERVIDOR/api/rutinas`. En el entorno de Postman configura `baseUrl=http://IP_SERVIDOR`, sin `:8080` ni `/api`, porque Nginx publica el puerto 80. Si cambias `HTTP_PORT`, incluye ese puerto en las direcciones.

El acceso público de esta configuración es HTTP. Para HTTPS, configura un dominio y un balanceador ALB con certificado ACM, o configura TLS en Nginx con un certificado válido. La conexión backend–RDS utiliza TLS con comprobación del certificado y del nombre del servidor.

No imprimas `docker compose config` sin `--quiet` al compartir diagnósticos: puede mostrar las credenciales resueltas. `.env` y las claves SSH quedan fuera de Git y del contexto de construcción.

Referencias: [TLS en RDS PostgreSQL](https://docs.aws.amazon.com/us_en/AmazonRDS/latest/UserGuide/PostgreSQL.Concepts.General.SSL.html) y [proxy de Nginx](https://nginx.org/en/docs/http/ngx_http_proxy_module.html).
