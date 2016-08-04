![khipu icon](https://khipu.com/assets/logo/logo-purple.png)

# demo-android

## Resumen

Aplicación de demo para integrar khipu en aplicaciones móviles

####  Pasos para pagar invocando khipu

1. **MiApp** genera el cobro solicita que tu servidor genere un cobro usando la [api REST de khipu](https://khipu.com/page/api).[^1]
2. **MiApp** invoca a la aplicación móvil **khipu** con la URL del cobro (campo app_url obtenido al crear el cobro).
3. La aplicación móvil **khipu** procesa el cobro 
4. La aplicación móvil **khipu**, al finalizar el cobro, invoca **MiApp** con el resultado del proceso usando la url return_url entregada al momento de crear el cobro.

[^1]: Para generar el cobro, tu aplicación móvil debe comunicarse con tu aplicación de servidor. Nunca lo hagas dentro de la misma aplicación móvil, pues hay información privada necesaria para generar el cobro.




