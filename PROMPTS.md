Herramienta: Claude. Prompt: "Vamos a construir paso a paso un sistema de pedidos de restaurante con JWT, roles y autorización por sucursal, usando PostgreSQL." Qué generó: configuración base de build.gradle y application.yaml con variables de entorno para DB y JWT. Corregido/validado: ninguna corrección necesaria en este paso, se verificó que los defaults no queden como secretos reales en el repo.

Prompt: "Crea las entidades del dominio para el sistema de pedidos: Usuario, Sucursal, Mesa, Producto, Pedido, ItemPedido, RefreshToken, con JPA." Qué generó: las 8 entidades con relaciones y un BaseEntity para auditoría. Decisión que tuve que entender y poder justificar: por qué Pedido guarda sucursal_id duplicado en vez de navegar mesa.sucursal (denormalización para simplificar las validaciones de autorización por sucursal que exige la Opción B), y por qué ItemPedido copia el precio en vez de referenciarlo siempre desde Producto (integridad histórica).

Prompt: "Crea los repositorios JPA para las entidades, incluyendo las consultas necesarias para filtrar por sucursal." Qué generó: 6 interfaces de repositorio con derived queries de Spring Data. Decisión clave a entender: findByIdAndSucursalId en PedidoRepository filtra la pertenencia a sucursal directamente en la consulta SQL, en vez de traer la entidad completa y comparar en Java — esto es lo que hace que la autorización por atributo sea eficiente y no se pueda "olvidar" en algún punto del código de servicio.

it commit -m "feat: agregar DTOs de request/response para evitar exponer entidades JPA en la API"










