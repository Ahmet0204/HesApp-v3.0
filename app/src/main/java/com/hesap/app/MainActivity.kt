package com.hesap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val KOLON_RENKLERI = listOf(
    Color(0xFFF5F0E8),
    Color(0xFFD0D0D0),
    Color(0xFFF5F0E8),
    Color(0xFFD0D0D0),
)
val KOLON_RENKLERI_KOYU = listOf(
    Color(0xFF2A2620),
    Color(0xFF222222),
    Color(0xFF2A2620),
    Color(0xFF222222),
)

val TOPRAK_BG        = Color(0xFF1C1208)
val TOPRAK_LOGO_BG   = Color(0xFFF5ECD7)
val TOPRAK_LOGO_YAZI = Color(0xFF8B1A1A)
val TOPRAK_BASLIK    = Color(0xFFD4A96A)
val TOPRAK_ALT_YAZI  = Color(0xFF7A6040)
val TOPRAK_BUTON_BG  = Color(0xFF2C1F0E)
val TOPRAK_BUTON_BR  = Color(0xFF8B6914)
val TOPRAK_VERSIYON  = Color(0xFF4A3520)
val TOPRAK_DEVELOPED = Color(0xFF5A4530)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = TOPRAK_BG) {
                    HesapUygulamasi()
                }
            }
        }
    }
}

@Composable
fun HesapUygulamasi() {
    var kolonSayisi by remember { mutableStateOf(0) }
    if (kolonSayisi == 0) KolonSecimEkrani { kolonSayisi = it }
    else HesapTablosu(kolonSayisi = kolonSayisi, onReset = { kolonSayisi = 0 })
}

@Composable
fun KolonSecimEkrani(onSecim: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(TOPRAK_BG)) {
        Text(
            text = "v3.2 Beta",
            fontSize = 11.sp,
            color = TOPRAK_VERSIYON,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 14.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier.size(130.dp).clip(CircleShape).background(TOPRAK_LOGO_BG),
                contentAlignment = Alignment.Center
            ) {
                Text("101", fontSize = 50.sp, fontWeight = FontWeight.Black, color = TOPRAK_LOGO_YAZI, textAlign = TextAlign.Center)
            }
            Text("HesApp", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = TOPRAK_BASLIK, letterSpacing = 4.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Oyun modunu seçin", fontSize = 15.sp, color = TOPRAK_ALT_YAZI, textAlign = TextAlign.Center)
                KolonButonu(label = "Ortaklı") { onSecim(2) }
                KolonButonu(label = "Herkes Tek") { onSecim(4) }
            }
        }
        Text(
            text = "Developed by Ahmet TOKMAK",
            fontSize = 10.sp,
            color = TOPRAK_DEVELOPED,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        )
    }
}

@Composable
fun KolonButonu(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.65f).height(58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = TOPRAK_BUTON_BG),
        border = androidx.compose.foundation.BorderStroke(2.dp, TOPRAK_BUTON_BR)
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TOPRAK_BASLIK, textAlign = TextAlign.Center)
    }
}

@Composable
fun HesapTablosu(kolonSayisi: Int, onReset: () -> Unit) {
    val INPUT_ROWS = 9
    val satirValues = remember(kolonSayisi) { Array(INPUT_ROWS) { Array(kolonSayisi) { mutableStateOf("") } } }
    val bitisCount = remember(kolonSayisi) { Array(INPUT_ROWS) { Array(kolonSayisi) { mutableStateOf(0) } } }
    val focusRequesters = remember(kolonSayisi) { Array(INPUT_ROWS) { Array(kolonSayisi) { FocusRequester() } } }

    var showSifirlaDialog by remember { mutableStateOf(false) }
    var showGeriDonDialog by remember { mutableStateOf(false) }

    fun sifirla() {
        for (row in 0 until INPUT_ROWS)
            for (col in 0 until kolonSayisi) {
                satirValues[row][col].value = ""
                bitisCount[row][col].value = 0
            }
    }

    val toplamlar by remember {
        derivedStateOf {
            List(kolonSayisi) { kolIdx ->
                var t = 0
                for (row in 0 until INPUT_ROWS) t += satirValues[row][kolIdx].value.toIntOrNull() ?: 0
                t
            }
        }
    }
    val maxToplam by remember { derivedStateOf { toplamlar.maxOrNull() ?: 0 } }

    val oyuncuBasliklari = if (kolonSayisi == 2)
        listOf("1. Oyuncu", "2. Oyuncu")
    else
        listOf("1. Oyuncu", "2. Oyuncu", "3. Oyuncu", "4. Oyuncu")

    val scrollState = rememberScrollState()

    if (showSifirlaDialog) {
        AlertDialog(
            onDismissRequest = { showSifirlaDialog = false },
            backgroundColor = Color(0xFF1A1A1A),
            title = { Text("Sayıları Sil", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Silmek istediğinizden emin misiniz?", color = Color(0xFFAAAAAA)) },
            confirmButton = {
                TextButton(onClick = { sifirla(); showSifirlaDialog = false }) {
                    Text("Evet, Sil", color = Color(0xFFCC0000), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSifirlaDialog = false }) {
                    Text("İptal", color = Color(0xFF888888))
                }
            }
        )
    }

    if (showGeriDonDialog) {
        AlertDialog(
            onDismissRequest = { showGeriDonDialog = false },
            backgroundColor = Color(0xFF1A1A1A),
            title = { Text("Ana Sayfaya Dön", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Ana sayfaya dönmek istediğinizden emin misiniz? Gittikten sonra tüm sayılar silinecektir.", color = Color(0xFFAAAAAA)) },
            confirmButton = {
                TextButton(onClick = { showGeriDonDialog = false; onReset() }) {
                    Text("Evet, Dön", color = Color(0xFFCC0000), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeriDonDialog = false }) {
                    Text("İptal", color = Color(0xFF888888))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF000000)).padding(top = 8.dp, bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { showGeriDonDialog = true }) {
                Text("← Geri Dön", color = Color(0xFF555555), fontSize = 13.sp)
            }
            TextButton(onClick = { showSifirlaDialog = true }) {
                Text("Sıfırla ↺", color = Color(0xFF555555), fontSize = 13.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 4.dp)) {
            Box(modifier = Modifier.width(24.dp))
            oyuncuBasliklari.forEachIndexed { idx, baslik ->
                val renk = KOLON_RENKLERI[idx]
                Box(
                    modifier = Modifier
                        .weight(1f).padding(horizontal = 2.dp)
                        .background(KOLON_RENKLERI_KOYU[idx], RoundedCornerShape(6.dp))
                        .border(1.dp, renk.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(baslik, fontSize = if (kolonSayisi == 2) 12.sp else 9.sp, fontWeight = FontWeight.Bold, color = renk, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState).padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (row in 0 until INPUT_ROWS) {
                GirisSatiri(
                    satirNo = row + 1,
                    kolonSayisi = kolonSayisi,
                    values = satirValues[row],
                    bitisCountRow = bitisCount[row],
                    focusRequesters = focusRequesters,
                    row = row,
                    inputRows = INPUT_ROWS,
                    onValueChange = { kolIdx, newVal -> satirValues[row][kolIdx].value = newVal },
                    onBitis = { kolIdx ->
                        when (bitisCount[row][kolIdx].value % 3) {
                            0 -> { satirValues[row][kolIdx].value = "-100"; bitisCount[row][kolIdx].value = 1 }
                            1 -> { satirValues[row][kolIdx].value = "-200"; bitisCount[row][kolIdx].value = 2 }
                            else -> { satirValues[row][kolIdx].value = ""; bitisCount[row][kolIdx].value = 0 }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            ToplamSatiri(kolonSayisi = kolonSayisi, toplamlar = toplamlar)
            Spacer(modifier = Modifier.height(2.dp))
            MaxSatiri(kolonSayisi = kolonSayisi, maxToplam = maxToplam, toplamlar = toplamlar)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GirisSatiri(
    satirNo: Int,
    kolonSayisi: Int,
    values: Array<MutableState<String>>,
    bitisCountRow: Array<MutableState<Int>>,
    focusRequesters: Array<Array<FocusRequester>>,
    row: Int,
    inputRows: Int,
    onValueChange: (Int, String) -> Unit,
    onBitis: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                Text("$satirNo", fontSize = 10.sp, color = Color(0xFF444444), textAlign = TextAlign.Center)
            }

            for (kolIdx in 0 until kolonSayisi) {
                val renk = KOLON_RENKLERI[kolIdx]
                val value = values[kolIdx].value
                val isBitis = value == "-100" || value == "-200"

                val nextFocus: FocusRequester? = when {
                    kolIdx < kolonSayisi - 1 -> focusRequesters[row][kolIdx + 1]
                    row < inputRows - 1 -> focusRequesters[row + 1][0]
                    else -> null
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .height(52.dp)
                        .background(if (isBitis) Color(0xFF2A1500) else renk, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isBitis) Color(0xFFFF6B35) else renk.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBitis) {
                        Text(
                            text = if (value == "-100") "BİTİŞ" else "BİTİŞ×2",
                            fontSize = 10.sp,
                            color = Color(0xFFFF6B35),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = { newVal ->
                                if (newVal.length <= 4 && (newVal.isEmpty() || newVal.all { it.isDigit() }))
                                    onValueChange(kolIdx, newVal)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesters[row][kolIdx]),
                            textStyle = TextStyle(
                                fontSize = 17.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { nextFocus?.requestFocus() },
                                onDone = {}
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFFFFD700))
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(24.dp))
            for (kolIdx in 0 until kolonSayisi) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 2.dp)) {
                    Button(
                        onClick = { onBitis(kolIdx) },
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A0800)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B35).copy(alpha = 0.5f))
                    ) {
                        Text("BİTİŞ", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
fun ToplamSatiri(kolonSayisi: Int, toplamlar: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFFCCCCCC), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            Text("∑", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        for (kolIdx in 0 until kolonSayisi) {
            Box(
                modifier = Modifier
                    .weight(1f).padding(horizontal = 2.dp).height(52.dp)
                    .background(KOLON_RENKLERI[kolIdx], RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFAAAAAA), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(toplamlar[kolIdx].toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun MaxSatiri(kolonSayisi: Int, maxToplam: Int, toplamlar: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0xFFCC0000), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF880000), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            Text("Δ", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        for (kolIdx in 0 until kolonSayisi) {
            val isEnYuksek = toplamlar[kolIdx] == maxToplam && maxToplam > 0
            val fark = maxToplam - toplamlar[kolIdx]
            Box(
                modifier = Modifier
                    .weight(1f).padding(horizontal = 2.dp).height(52.dp)
                    .background(if (isEnYuksek) Color(0xFF880000) else Color(0xFFAA0000), RoundedCornerShape(6.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$fark", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
            }
        }
    }
}
