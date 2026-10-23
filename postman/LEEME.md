# Importar en Postman: Docker local, puerto 8080

1. Inicia el proyecto con `iniciar-docker.cmd` desde la raíz del proyecto.
2. En tu workspace de Postman, pulsa **Import** y selecciona **el archivo** `Gimnasio-Sara-Local-8080.postman_collection.json` de esta carpeta. También puedes arrastrarlo a la ventana de importación.
3. En **Collections**, abre **Gimnasio Sara - Local 8080**. No aparecerá con el nombre del archivo ni con el de la base de datos.
4. Selecciona **No environment**. Esta colección usa direcciones completas, por lo que no requiere importar un entorno ni configurar `baseUrl`.
5. Abre **01 · Salud del despliegue** y pulsa **Send**: la dirección es `http://localhost:8080/actuator/health` y debe devolver HTTP 200 con `status: UP`.
6. Para comprobar todo el CRUD, ejecuta las 30 peticiones en orden mediante **Run collection**. Los IDs se guardan automáticamente al crear los recursos. Si envías las peticiones con IDs antes de ejecutar las de creación, faltarán esas variables.

El archivo `compose.yaml` configura Docker; no es una colección importable en Postman. `AWS.postman_environment.json` es solo un entorno y no crea una colección en la sección Collections. El archivo que debes importar para esta prueba es el que termina en `.postman_collection.json` y contiene `Local-8080` en su nombre.

## Si sigue fallando

- **La colección no aparece**: comprueba que estás mirando Collections en el mismo workspace donde la importaste y busca `Gimnasio Sara - Local 8080`.
- **No puede acceder a localhost desde Postman web**: utiliza la aplicación de escritorio de Postman o conecta y selecciona el Desktop Agent. El agente debe ejecutarse en la misma PC que Docker Desktop.
- **ECONNREFUSED**: ejecuta `iniciar-docker.cmd` y comprueba `http://localhost:8080/actuator/health` en el navegador.
- **URL con `{{ejercicioA}}` o `{{rutinaA}}` sin resolver**: ejecuta la colección en orden; las peticiones de creación guardan esos IDs.

Para AWS conserva `Gimnasio-Sara-AWS.postman_collection.json` y configura su entorno con la URL pública real cuando publiques el servicio.

Documentación oficial: [importar archivos](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-data) y [aplicación y Desktop Agent](https://learning.postman.com/docs/getting-started/installation/install-app).
