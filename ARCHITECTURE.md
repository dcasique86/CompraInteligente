# Compra Inteligente — arquitectura inicial

La aplicación se organiza en capas para poder reemplazar el origen local por Firebase sin modificar las pantallas.

```
ui/           Pantallas Compose y navegación
viewmodel/    Estado y acciones de pantalla
domain/       Modelo `ProductOpportunity` y contrato `ProductRepository`
data/local/   Implementación temporal `InMemoryProductRepository`
di/           `AppContainer`, único punto de selección del repositorio
```

## Migración posterior a Firebase

1. Crear `FirestoreProductRepository` en `data/remote/` que implemente `ProductRepository`.
2. Configurar Firebase/Firestore y sus reglas de acceso.
3. Sustituir una única línea en `AppContainer` para inyectar el repositorio de Firestore.

El conversor y su historial Room existente se conservan independientes en `data/` y `viewmodel/ConverterViewModel.kt`.
