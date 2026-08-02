package com.example.ui.products

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberImagePainter
import com.example.domain.model.ProductCurrency
import com.example.domain.model.ProductPhoto
import com.example.viewmodel.NewProductViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: NewProductViewModel
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(ProductCurrency.USD) }
    var store by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    val parsedPrice = parsePrice(price)
    val canSave = name.isNotBlank() && category.isNotBlank() && parsedPrice != null && parsedPrice > 0 && store.isNotBlank()
    
    val capturedPhotos by viewModel.capturedPhotos.collectAsStateWithLifecycle()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            viewModel.onPhotoCaptured(photoUri!!)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoCaptured(it) }
    }

    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "Pictures")
        val imageFile = File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
        photoUri = Uri.fromFile(imageFile)
        return photoUri!!
    }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        CenterAlignedTopAppBar(
            title = { Text("Nuevo producto") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PhotoSection(
                capturedPhotos = capturedPhotos,
                onTakePhoto = { 
                    takePictureLauncher.launch(createImageFile())
                },
                onPickImage = { pickImageLauncher.launch(PickVisualMedia.ImageOnly) },
                onRemovePhoto = { photoId -> viewModel.removeCapturedPhoto(photoId) }
            )
            
            Field(name, { name = it }, "Nombre del producto", "Ej. Cargador iPhone 20W Original", showErrors && name.isBlank())
            Field(category, { category = it }, "Categoría", "Ej. Cargadores, Audífonos, Starlink", showErrors && category.isBlank())
            Field(price, { price = it }, "Precio en Venezuela", "Ej. 32", showErrors && (parsedPrice == null || parsedPrice <= 0), KeyboardType.Decimal)
            Text("Moneda", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductCurrency.entries.forEach { item ->
                    FilterChip(selected = currency == item, onClick = { currency = item }, label = { Text("${item.symbol} ${item.label}") })
                }
            }
            Field(store, { store = it }, "Tienda", "Ej. iCenter", showErrors && store.isBlank())
            Field(notes, { notes = it }, "Notas (opcional)", "Ej. Caja sellada, color disponible", false, KeyboardType.Text, minLines = 3)
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                onClick = {
                    showErrors = true
                    if (canSave) viewModel.save(name, category, parsedPrice!!, currency, store, notes, { onBack() })
                }
            ) { Text("Guardar producto") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhotoSection(
    capturedPhotos: List<ProductPhoto>,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (capturedPhotos.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                capturedPhotos.forEach { photo ->
                    PhotoThumbnail(
                        photo = photo,
                        onRemove = { onRemovePhoto(photo.id) }
                    )
                }
                AddPhotoButton(onTakePhoto = onTakePhoto, onPickImage = onPickImage)
            }
        } else {
            AddPhotoButton(onTakePhoto = onTakePhoto, onPickImage = onPickImage)
        }
    }
}

@Composable
fun PhotoThumbnail(
    photo: ProductPhoto,
    onRemove: () -> Unit
) {
    val thumbnailPath = photo.thumbnailPath ?: photo.localPath
    
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = rememberImagePainter(thumbnailPath),
            contentDescription = "Foto del producto",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar foto", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
        
        if (!photo.synced) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        "Pendiente de sincronizar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPhotoButton(onTakePhoto: () -> Unit, onPickImage: () -> Unit) {
    Surface(
        modifier = Modifier.size(100.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Agregar foto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTakePhoto) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cámara", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPickImage) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galería", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, isError: Boolean, keyboardType: KeyboardType = KeyboardType.Text, minLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, placeholder = { Text(placeholder) }, singleLine = minLines == 1,
        minLines = minLines, isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = if (isError) ({ Text("Este campo es obligatorio") }) else null,
        colors = OutlinedTextFieldDefaults.colors()
    )
}

private fun parsePrice(input: String): Double? = input.trim().replace(',', '.').toDoubleOrNull()