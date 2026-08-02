# CompraInteligente

Aplicación Android (Kotlin + Jetpack Compose) para registrar **oportunidades de compra**: capturas el precio de un producto (en Venezuela), su tienda, categoría y fotos, para compararlo luego con el costo en Colombia y decidir si conviene comprar.

## Características

- Registro de productos con nombre, categoría, precio y tienda.
- Moneda en **USD** o **Bolívares (Bs.)**.
- Fotos por producto (tomadas de galería/cámara) con sincronización en segundo plano.
- Convertidor de moneda integrado.
- Base de datos local (Room) con persistencia.
- Arquitectura limpia: `domain`, `data`, `ui` y `viewmodel`.

## Requisitos

- [Android Studio](https://developer.android.com/studio)
- JDK 17 (incluido con Android Studio)
- Android SDK

## Configuración

1. Abre Android Studio → **Open** y selecciona la carpeta del proyecto.
2. Deja que Gradle sincronice el proyecto.
3. Crea un archivo `.env` en la raíz con tu clave (ver `.env.example`).
4. Ejecuta en un emulador o dispositivo físico.

## Estructura

```
CompraInteligente/
├── app/src/main/java/com/example/
│   ├── data/            ← Room, repositorios, sincronización de fotos
│   ├── domain/          ← Modelos y contratos de repositorio
│   ├── ui/              ← Pantallas Compose y navegación
│   ├── viewmodel/       ← ViewModels
│   └── di/              ← Inyección de dependencias
├── assets/              ← Recursos
└── build.gradle.kts     ← Configuración Gradle
```

## Compilar APK

```bat
build_app.bat
```

## Notas

- El archivo `local.properties` y `debug.keystore` son locales y **no** se suben al repositorio.
- La app fue construida como parte del ecosistema Google AI Studio (Gemini).
