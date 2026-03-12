package com.hesap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val KOLON_RENKLERI = listOf(
    Color(0xFFF5F0E8), // Krem
    Color(0xFFD0D0D0), // Gri
    Color(0xFFF5F0E8), // Krem
    Color(0xFFD0D0D0), // Gri
)
val KOLON_RENKLERI_KOYU = listOf(
    Color(0xFF2A2620), // Krem koyu
    Color(0xFF222222), // Gri koyu
    Color(0xFF2A2620),
    Color(0xFF222222),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000000)) {
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
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst 1/3: Logo
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.33f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "101",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFCC0000),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Alt 2/3: İsim ve butonlar
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("HesApp", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Oyun modunu seçin", fontSize = 15.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(36.dp))
                KolonButonu(label = "Ortaklı") { onSecim(2) }
                Spacer(modifier = Modifier.height(16.dp))
                KolonButonu(label = "Herkes Tek") { onSecim(4) }
            }
        }
        // Versiyon - sağ alt köşe
        Text(
            text = "v3.1.1 Beta",
            fontSize = 10.sp,
            color = Color(0xFF333333),
            modifier = androidx.compose.ui.Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun KolonButonu(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.65f).height(58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A1A1A)),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun HesapTablosu(kolonSayisi: Int, onReset: () -> Unit) {
    val INPUT_ROWS = 9
    val satirValues = remember(kolonSayisi) { Array(INPUT_ROWS) { Array(kolonSayisi) { mutableStateOf("") } } }
    val bitisCount = remember(kolonSayisi) { Array(INPUT_ROWS) { Array(kolonSayisi) { mutableStateOf(0) } } }

    // FocusRequester grid: [row][col]
    val focusRequesters = remember(kolonSayisi) {
        Array(INPUT_ROWS) { Array(kolonSayisi) { FocusRequester() } }
    }

    fun kolonToplam(kolIndex: Int): Int {
        var t = 0
        for (row in 0 until INPUT_ROWS) t += satirValues[row][kolIndex].value.toIntOrNull() ?: 0
        return t
    }

    val toplamlar = List(kolonSayisi) { kolonToplam(it) }
    val maxToplam = toplamlar.maxOrNull() ?: 0

    fun satirMaxDegeri(row: Int): Int {
        var max = Int.MIN_VALUE
        for (k in 0 until kolonSayisi) {
            val v = satirValues[row][k].value.toIntOrNull() ?: Int.MIN_VALUE
            if (v > max) max = v
        }
        return max
    }

    val oyuncuBasliklari = if (kolonSayisi == 2)
        listOf("1. Oyuncu", "2. Oyuncu")
    else
        listOf("1. Oyuncu", "2. Oyuncu", "3. Oyuncu", "4. Oyuncu")

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF000000)).padding(top = 8.dp, bottom = 8.dp)) {

        Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onReset) {
                Text("↩ Sıfırla", color = Color(0xFF555555), fontSize = 13.sp)
            }
        }

        // Oyuncu başlıkları
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 4.dp)) {
            Box(modifier = Modifier.width(24.dp))
            oyuncuBasliklari.forEachIndexed { idx, baslik ->
                val renk = KOLON_RENKLERI[idx]
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .background(KOLON_RENKLERI_KOYU[idx], RoundedCornerShape(6.dp))
                        .border(1.dp, renk.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        baslik,
                        fontSize = if (kolonSayisi == 2) 12.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = renk,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState).padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (row in 0 until INPUT_ROWS) {
                val satirMax = satirMaxDegeri(row)
                GirisSatiri(
                    satirNo = row + 1,
                    kolonSayisi = kolonSayisi,
                    values = satirValues[row],
                    bitisCountRow = bitisCount[row],
                    satirMax = satirMax,
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
            MaxSatiri(kolonSayisi = kolonSayisi, satirValues = satirValues, inputRows = INPUT_ROWS, maxToplam = maxToplam, toplamlar = toplamlar)
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
    satirMax: Int,
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
                val renkKoyu = KOLON_RENKLERI_KOYU[kolIdx]
                val value = values[kolIdx].value
                val isBitis = value == "-100" || value == "-200"
                val cellValue = value.toIntOrNull()
                val isMax = cellValue != null && cellValue == satirMax && satirMax != Int.MIN_VALUE

                // Enter tuşuna basıldığında bir sağdaki hücreye geç
                val nextFocus: FocusRequester? = when {
                    kolIdx < kolonSayisi - 1 -> focusRequesters[row][kolIdx + 1]
                    row < inputRows - 1 -> focusRequesters[row + 1][0]
                    else -> null
                }

                Box(modifier = Modifier.weight(1f).padding(horizontal = 2.dp)) {
                    OutlinedTextField(
                        value = if (isBitis) "" else value,
                        onValueChange = { newVal ->
                            if (newVal.length <= 4 && (newVal.isEmpty() || newVal.all { it.isDigit() }))
                                onValueChange(kolIdx, newVal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .focusRequester(focusRequesters[row][kolIdx]),
                        placeholder = {
                            if (isBitis) Text(
                                if (value == "-100") "BİTİŞ" else "BİTİŞ×2",
                                fontSize = 9.sp,
                                color = Color(0xFFFF6B35),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = if (isMax) Color(0xFFFFD700) else Color.Black,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal
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
                        shape = RoundedCornerShape(6.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = if (isBitis) Color(0xFFFF6B35) else renk.copy(alpha = 0.6f),
                            cursorColor = Color(0xFFFFD700),
                            textColor = if (isMax) Color(0xFFFFD700) else Color.Black,
                            backgroundColor = if (isBitis) Color(0xFF2A1500) else renk
                        )
                    )
                }
            }
        }

        // BİTİŞ butonları
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
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(52.dp)
                    .background(KOLON_RENKLERI[kolIdx], RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFAAAAAA), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    toplamlar[kolIdx].toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MaxSatiri(kolonSayisi: Int, satirValues: Array<Array<MutableState<String>>>, inputRows: Int, maxToplam: Int, toplamlar: List<Int>) {
    fun kolonMaxSatirDegeri(kolIdx: Int): Int {
        var max = Int.MIN_VALUE
        for (row in 0 until inputRows) {
            val v = satirValues[row][kolIdx].value.toIntOrNull() ?: Int.MIN_VALUE
            if (v > max) max = v
        }
        return if (max == Int.MIN_VALUE) 0 else max
    }

    val kolonMaxlari = List(kolonSayisi) { kolonMaxSatirDegeri(it) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0xFFCC0000), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF880000), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            Text("↑", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        for (kolIdx in 0 until kolonSayisi) {
            val isEnYuksek = toplamlar[kolIdx] == maxToplam && maxToplam > 0
            val fark = maxToplam - toplamlar[kolIdx]
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(52.dp)
                    .background(if (isEnYuksek) Color(0xFF880000) else Color(0xFFAA0000), RoundedCornerShape(6.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnYuksek) kolonMaxlari[kolIdx].toString() else "$fark",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
