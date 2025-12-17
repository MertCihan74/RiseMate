package com.parthenios.risemate.data

/**
 * Her yetenek için 10 soru içeren quiz veri kaynağı
 * Kullanıcıya 5 rastgele soru sorulur, 4 doğru yapması gerekir
 */
object SkillQuizData {
    
    // Geçme kriteri: 5 sorudan 4'ü doğru
    const val QUESTIONS_PER_QUIZ = 5
    const val PASS_THRESHOLD = 4
    
    private val allQuestions = mapOf(
        // ========== YAZILIM DİLLERİ ==========
        "Python" to listOf(
            QuizQuestion("py1", "Python'da liste oluşturmak için hangi sözdizimi kullanılır?", listOf("list{}", "[]", "array()", "<>"), 1, "Python"),
            QuizQuestion("py2", "Python'da fonksiyon tanımlamak için hangi anahtar kelime kullanılır?", listOf("function", "func", "def", "fn"), 2, "Python"),
            QuizQuestion("py3", "Python'da yorum satırı nasıl başlar?", listOf("//", "/*", "#", "--"), 2, "Python"),
            QuizQuestion("py4", "Python'da 'print' fonksiyonunun çıktısı nereye gider?", listOf("Dosyaya", "Konsola", "Veritabanına", "Ağa"), 1, "Python"),
            QuizQuestion("py5", "Python'da dictionary (sözlük) hangi parantezle tanımlanır?", listOf("[]", "()", "{}", "<>"), 2, "Python"),
            QuizQuestion("py6", "Python'da string birleştirme operatörü hangisidir?", listOf("&", "+", ".", "++"), 1, "Python"),
            QuizQuestion("py7", "'len()' fonksiyonu ne döndürür?", listOf("Veri tipi", "Uzunluk", "Son eleman", "İlk eleman"), 1, "Python"),
            QuizQuestion("py8", "Python'da 'None' neyi temsil eder?", listOf("Sıfır", "Boş string", "Değer yok", "Hata"), 2, "Python"),
            QuizQuestion("py9", "Python'da döngü için hangi anahtar kelime kullanılır?", listOf("loop", "for", "repeat", "iterate"), 1, "Python"),
            QuizQuestion("py10", "Python'da hata yakalamak için hangi yapı kullanılır?", listOf("catch-throw", "try-except", "error-handle", "if-else"), 1, "Python")
        ),
        
        "Java" to listOf(
            QuizQuestion("java1", "Java'da main metodu nasıl tanımlanır?", listOf("public void main()", "public static void main(String[] args)", "static main()", "void main()"), 1, "Java"),
            QuizQuestion("java2", "Java'da yeni nesne oluşturmak için hangi anahtar kelime kullanılır?", listOf("create", "new", "make", "init"), 1, "Java"),
            QuizQuestion("java3", "Java'da sınıf tanımlamak için hangi anahtar kelime kullanılır?", listOf("struct", "type", "class", "object"), 2, "Java"),
            QuizQuestion("java4", "Java'da kalıtım için hangi anahtar kelime kullanılır?", listOf("inherits", "extends", "implements", "derives"), 1, "Java"),
            QuizQuestion("java5", "Java'da interface uygulamak için hangi anahtar kelime kullanılır?", listOf("extends", "inherits", "implements", "uses"), 2, "Java"),
            QuizQuestion("java6", "Java'da dizi tanımlamak için hangi sözdizimi doğrudur?", listOf("int arr[]", "array int[]", "int[] = new", "arr = []"), 0, "Java"),
            QuizQuestion("java7", "Java'da 'final' anahtar kelimesi ne anlama gelir?", listOf("Son metod", "Değiştirilemez", "Soyut", "Genel"), 1, "Java"),
            QuizQuestion("java8", "Java'da 'this' anahtar kelimesi neyi ifade eder?", listOf("Üst sınıf", "Mevcut nesne", "Ana metod", "Statik değişken"), 1, "Java"),
            QuizQuestion("java9", "Java'da paket bildirimi nasıl yapılır?", listOf("import", "package", "include", "using"), 1, "Java"),
            QuizQuestion("java10", "Java'da boolean veri tipi hangi değerleri alabilir?", listOf("0 veya 1", "true veya false", "yes veya no", "on veya off"), 1, "Java")
        ),
        
        "JavaScript" to listOf(
            QuizQuestion("js1", "JavaScript'te değişken tanımlamak için hangisi kullanılmaz?", listOf("var", "let", "const", "int"), 3, "JavaScript"),
            QuizQuestion("js2", "JavaScript'te '===' operatörü ne yapar?", listOf("Atama", "Tip ve değer karşılaştırma", "Sadece değer karşılaştırma", "Referans karşılaştırma"), 1, "JavaScript"),
            QuizQuestion("js3", "JavaScript'te arrow function nasıl yazılır?", listOf("-> {}", "=> {}", ">> {}", "-- {}"), 1, "JavaScript"),
            QuizQuestion("js4", "JavaScript'te 'null' ve 'undefined' arasındaki fark nedir?", listOf("Aynı şey", "null bilinçli, undefined otomatik", "undefined bilinçli", "Fark yok"), 1, "JavaScript"),
            QuizQuestion("js5", "JavaScript'te DOM manipülasyonu için hangi metod kullanılır?", listOf("getElement()", "querySelector()", "findDOM()", "selectHTML()"), 1, "JavaScript"),
            QuizQuestion("js6", "JavaScript'te asenkron işlem için hangisi kullanılır?", listOf("sync()", "await/async", "parallel()", "thread()"), 1, "JavaScript"),
            QuizQuestion("js7", "JavaScript'te dizi sonuna eleman eklemek için hangi metod kullanılır?", listOf("add()", "append()", "push()", "insert()"), 2, "JavaScript"),
            QuizQuestion("js8", "JavaScript'te 'typeof' operatörü ne döndürür?", listOf("Değişken adı", "Veri tipi", "Değer", "Uzunluk"), 1, "JavaScript"),
            QuizQuestion("js9", "JavaScript'te JSON parse etmek için hangi metod kullanılır?", listOf("JSON.decode()", "JSON.parse()", "JSON.convert()", "JSON.read()"), 1, "JavaScript"),
            QuizQuestion("js10", "JavaScript'te event listener eklemek için hangi metod kullanılır?", listOf("onEvent()", "addListener()", "addEventListener()", "bindEvent()"), 2, "JavaScript")
        ),
        
        "Kotlin" to listOf(
            QuizQuestion("kt1", "Kotlin'de nullable değişken nasıl tanımlanır?", listOf("String!", "String?", "String*", "?String"), 1, "Kotlin"),
            QuizQuestion("kt2", "Kotlin'de veri sınıfı tanımlamak için hangi anahtar kelime kullanılır?", listOf("struct", "data class", "record", "model"), 1, "Kotlin"),
            QuizQuestion("kt3", "Kotlin'de fonksiyon tanımlamak için hangi anahtar kelime kullanılır?", listOf("function", "def", "fun", "func"), 2, "Kotlin"),
            QuizQuestion("kt4", "Kotlin'de değişmez değişken için hangi anahtar kelime kullanılır?", listOf("const", "val", "let", "final"), 1, "Kotlin"),
            QuizQuestion("kt5", "Kotlin'de değişebilir değişken için hangi anahtar kelime kullanılır?", listOf("var", "let", "mut", "dynamic"), 0, "Kotlin"),
            QuizQuestion("kt6", "Kotlin'de string interpolation nasıl yapılır?", listOf("\${}", "{}", "#{}", "%s"), 0, "Kotlin"),
            QuizQuestion("kt7", "Kotlin'de 'when' ifadesi neyin karşılığıdır?", listOf("if-else", "switch-case", "for döngüsü", "while döngüsü"), 1, "Kotlin"),
            QuizQuestion("kt8", "Kotlin'de companion object ne işe yarar?", listOf("Singleton oluşturma", "Statik üyeler", "Inheritance", "Interface"), 1, "Kotlin"),
            QuizQuestion("kt9", "Kotlin'de extension function ne sağlar?", listOf("Yeni sınıf oluşturma", "Mevcut sınıfa fonksiyon ekleme", "Kalıtım", "Interface"), 1, "Kotlin"),
            QuizQuestion("kt10", "Kotlin'de coroutine için hangi anahtar kelime kullanılır?", listOf("async", "thread", "suspend", "parallel"), 2, "Kotlin")
        ),
        
        "React" to listOf(
            QuizQuestion("react1", "React'te component oluşturmanın modern yolu hangisidir?", listOf("Class component", "Function component", "Object component", "Module component"), 1, "React"),
            QuizQuestion("react2", "React'te state yönetimi için hangi hook kullanılır?", listOf("useEffect", "useState", "useContext", "useReducer"), 1, "React"),
            QuizQuestion("react3", "React'te side effect'ler için hangi hook kullanılır?", listOf("useState", "useEffect", "useMemo", "useCallback"), 1, "React"),
            QuizQuestion("react4", "JSX nedir?", listOf("JavaScript kütüphanesi", "JavaScript XML", "JSON extension", "Java syntax"), 1, "React"),
            QuizQuestion("react5", "React'te props ne işe yarar?", listOf("State yönetimi", "Component'e veri geçirme", "Event handling", "Routing"), 1, "React"),
            QuizQuestion("react6", "React'te key prop'u ne için kullanılır?", listOf("Styling", "Liste render optimizasyonu", "Event binding", "State yönetimi"), 1, "React"),
            QuizQuestion("react7", "React'te virtual DOM'un avantajı nedir?", listOf("Daha kolay kod", "Performans", "SEO", "Güvenlik"), 1, "React"),
            QuizQuestion("react8", "React'te conditional rendering nasıl yapılır?", listOf("if-else JSX içinde", "&& veya ternary operatör", "switch-case", "for döngüsü"), 1, "React"),
            QuizQuestion("react9", "React'te context API ne için kullanılır?", listOf("HTTP istekleri", "Global state yönetimi", "Routing", "Animation"), 1, "React"),
            QuizQuestion("react10", "React'te useRef hook'u ne için kullanılır?", listOf("State yönetimi", "DOM referansı", "Effect cleanup", "Context"), 1, "React")
        ),
        
        // ========== MOBİL GELİŞTİRME ==========
        "Android" to listOf(
            QuizQuestion("and1", "Android'de Activity'nin yaşam döngüsünde hangi metod ilk çağrılır?", listOf("onStart()", "onCreate()", "onResume()", "onInit()"), 1, "Android"),
            QuizQuestion("and2", "Android'de layout dosyaları hangi klasörde bulunur?", listOf("src/", "res/layout/", "assets/", "lib/"), 1, "Android"),
            QuizQuestion("and3", "Android'de Intent ne için kullanılır?", listOf("Veri depolama", "Activity geçişi ve veri aktarımı", "UI tasarımı", "Network işlemleri"), 1, "Android"),
            QuizQuestion("and4", "Android'de RecyclerView ne için kullanılır?", listOf("Resim gösterme", "Liste görüntüleme", "Navigation", "Database"), 1, "Android"),
            QuizQuestion("and5", "Android'de Fragment ne işe yarar?", listOf("Veritabanı işlemleri", "Yeniden kullanılabilir UI parçası", "Network", "Storage"), 1, "Android"),
            QuizQuestion("and6", "Android'de SharedPreferences ne için kullanılır?", listOf("Büyük veri depolama", "Küçük anahtar-değer depolama", "Resim depolama", "Database"), 1, "Android"),
            QuizQuestion("and7", "Android'de ViewModel ne sağlar?", listOf("UI tasarımı", "Lifecycle-aware veri yönetimi", "Network", "Notification"), 1, "Android"),
            QuizQuestion("and8", "Android'de Gradle ne için kullanılır?", listOf("UI tasarımı", "Build sistemi", "Database", "Testing"), 1, "Android"),
            QuizQuestion("and9", "Android'de manifest dosyası ne içerir?", listOf("UI layout", "Uygulama konfigürasyonu", "Database schema", "Assets"), 1, "Android"),
            QuizQuestion("and10", "Android'de LiveData ne sağlar?", listOf("Network calls", "Lifecycle-aware observable", "Database", "UI components"), 1, "Android")
        ),
        
        "Flutter" to listOf(
            QuizQuestion("fl1", "Flutter hangi programlama dilini kullanır?", listOf("Java", "Kotlin", "Dart", "Swift"), 2, "Flutter"),
            QuizQuestion("fl2", "Flutter'da her şey nedir?", listOf("Function", "Widget", "Component", "Module"), 1, "Flutter"),
            QuizQuestion("fl3", "Flutter'da StatelessWidget ne zaman kullanılır?", listOf("Değişen veri varsa", "Sabit UI için", "Network için", "Database için"), 1, "Flutter"),
            QuizQuestion("fl4", "Flutter'da state güncellemek için hangi metod çağrılır?", listOf("update()", "refresh()", "setState()", "rebuild()"), 2, "Flutter"),
            QuizQuestion("fl5", "Flutter'da hot reload ne sağlar?", listOf("Performans artışı", "Anlık kod değişikliği görme", "Database sync", "Network cache"), 1, "Flutter"),
            QuizQuestion("fl6", "Flutter'da Scaffold widget'ı ne sağlar?", listOf("Liste görünümü", "Temel sayfa yapısı", "Network", "Database"), 1, "Flutter"),
            QuizQuestion("fl7", "Flutter'da pubspec.yaml ne içerir?", listOf("UI kodu", "Proje bağımlılıkları", "Database schema", "Test files"), 1, "Flutter"),
            QuizQuestion("fl8", "Flutter'da Navigator ne için kullanılır?", listOf("Veri depolama", "Sayfa geçişleri", "Network", "Animation"), 1, "Flutter"),
            QuizQuestion("fl9", "Flutter'da FutureBuilder ne için kullanılır?", listOf("UI tasarımı", "Asenkron veri ile widget oluşturma", "Database", "Cache"), 1, "Flutter"),
            QuizQuestion("fl10", "Flutter'da Container widget'ı ne sağlar?", listOf("Liste görünümü", "Padding, margin, decoration", "Navigation", "Network"), 1, "Flutter")
        ),
        
        // ========== MÜZİK ==========
        "Gitar" to listOf(
            QuizQuestion("gt1", "Standart gitar akordu (6. telden 1. tele) hangisidir?", listOf("D-A-D-G-B-E", "E-A-D-G-B-E", "E-A-D-G-A-E", "D-G-D-G-B-E"), 1, "Gitar"),
            QuizQuestion("gt2", "Barre akor nedir?", listOf("Açık akor", "Tek parmakla tüm tellere basma", "Arpej çalma", "Pena tekniği"), 1, "Gitar"),
            QuizQuestion("gt3", "Capo ne işe yarar?", listOf("Ses yükseltme", "Ton değiştirme/yükseltme", "Ses kısma", "Teli gevşetme"), 1, "Gitar"),
            QuizQuestion("gt4", "Bir oktavda kaç yarım ton vardır?", listOf("7", "8", "10", "12"), 3, "Gitar"),
            QuizQuestion("gt5", "Am (La minör) akoru hangi notalardan oluşur?", listOf("A-C-E", "A-C#-E", "A-D-E", "A-B-E"), 0, "Gitar"),
            QuizQuestion("gt6", "Gitarda perde (fret) sayısı genellikle kaçtır?", listOf("12-15", "19-24", "8-10", "25-30"), 1, "Gitar"),
            QuizQuestion("gt7", "Fingerstyle tekniği nedir?", listOf("Pena ile çalma", "Parmaklarla tel çekme", "Slide tekniği", "Barre akor"), 1, "Gitar"),
            QuizQuestion("gt8", "G majör akoru hangi notalardan oluşur?", listOf("G-A-D", "G-B-D", "G-C-E", "G-D-A"), 1, "Gitar"),
            QuizQuestion("gt9", "Hammer-on tekniği nedir?", listOf("Tele vurmadan basma", "Pena ile hızlı çalma", "Slide yapma", "Tel bükme"), 0, "Gitar"),
            QuizQuestion("gt10", "Gitarda standart E teli hangi frekanstadır?", listOf("220 Hz", "330 Hz", "440 Hz", "82.4 Hz"), 3, "Gitar")
        ),
        
        "Piyano" to listOf(
            QuizQuestion("pn1", "Standart piyanoda kaç tuş vardır?", listOf("61", "76", "88", "92"), 2, "Piyano"),
            QuizQuestion("pn2", "Orta Do (Middle C) hangi oktavdadır?", listOf("3. oktav", "4. oktav", "5. oktav", "2. oktav"), 1, "Piyano"),
            QuizQuestion("pn3", "Sol anahtarı hangi notayı gösterir?", listOf("Do", "Fa", "Sol", "La"), 2, "Piyano"),
            QuizQuestion("pn4", "Diyez (#) işareti ne yapar?", listOf("Yarım ton düşürür", "Yarım ton yükseltir", "Bir ton yükseltir", "Sesi uzatır"), 1, "Piyano"),
            QuizQuestion("pn5", "Majör ve minör akorlar arasındaki fark nedir?", listOf("Tuş sayısı", "3. derecenin yarım ton farkı", "Tempo", "Dinamik"), 1, "Piyano"),
            QuizQuestion("pn6", "Pedal (sustain) ne işe yarar?", listOf("Sesi kısar", "Notaları uzatır", "Tempo değiştirir", "Oktav değiştirir"), 1, "Piyano"),
            QuizQuestion("pn7", "C majör gamında kaç nota vardır?", listOf("5", "6", "7", "8"), 3, "Piyano"),
            QuizQuestion("pn8", "Fortissimo (ff) ne anlama gelir?", listOf("Çok yavaş", "Çok hızlı", "Çok güçlü/yüksek ses", "Çok hafif"), 2, "Piyano"),
            QuizQuestion("pn9", "Arpej nedir?", listOf("Aynı anda tüm notalar", "Notaların sırayla çalınması", "Ritim kalıbı", "Tempo işareti"), 1, "Piyano"),
            QuizQuestion("pn10", "Bemol (♭) işareti ne yapar?", listOf("Yarım ton yükseltir", "Yarım ton düşürür", "Sesi uzatır", "Sesi kısar"), 1, "Piyano")
        ),
        
        // ========== SPOR ==========
        "Futbol" to listOf(
            QuizQuestion("fb1", "Bir futbol maçı kaç dakikadır?", listOf("80 dk", "90 dk", "100 dk", "120 dk"), 1, "Futbol"),
            QuizQuestion("fb2", "Ofsayt kuralı ne zaman geçerlidir?", listOf("Top kaledeyken", "Pas anında son savunmacının gerisinde", "Korner atışında", "Taç atışında"), 1, "Futbol"),
            QuizQuestion("fb3", "Penaltı noktası kale çizgisine kaç metre uzaklıktadır?", listOf("9 metre", "11 metre", "12 metre", "16 metre"), 1, "Futbol"),
            QuizQuestion("fb4", "Sarı kart ne anlama gelir?", listOf("Oyundan atılma", "Uyarı", "Gol iptali", "Penaltı"), 1, "Futbol"),
            QuizQuestion("fb5", "Bir takımda sahada kaç oyuncu bulunur?", listOf("9", "10", "11", "12"), 2, "Futbol"),
            QuizQuestion("fb6", "Korner atışı ne zaman verilir?", listOf("Top yandan çıktığında", "Savunan takım topu kendi kalesinden çıkardığında", "Ofsayt olduğunda", "Faul yapıldığında"), 1, "Futbol"),
            QuizQuestion("fb7", "Hat-trick ne demektir?", listOf("Üç asist", "Bir maçta üç gol", "Üç sarı kart", "Üç penaltı"), 1, "Futbol"),
            QuizQuestion("fb8", "VAR ne anlama gelir?", listOf("Video Assistant Referee", "Visual Audio Recording", "Variable Action Review", "Virtual Assist Replay"), 0, "Futbol"),
            QuizQuestion("fb9", "Ceza sahası (penaltı alanı) ölçüleri nedir?", listOf("16.5m x 40.3m", "18.3m x 45m", "11m x 35m", "20m x 50m"), 0, "Futbol"),
            QuizQuestion("fb10", "Futbolda el kuralı hangi durumda geçerli değildir?", listOf("Tüm oyuncularda", "Kalecide kendi ceza sahasında", "Kaptanda", "Defans oyuncularında"), 1, "Futbol")
        ),
        
        "Basketbol" to listOf(
            QuizQuestion("bb1", "NBA'de bir periyot kaç dakikadır?", listOf("10 dk", "12 dk", "15 dk", "8 dk"), 1, "Basketbol"),
            QuizQuestion("bb2", "Üç sayılık çizgisi NBA'de kaç metre uzaklıktadır?", listOf("6.25m", "6.75m", "7.24m", "5.80m"), 2, "Basketbol"),
            QuizQuestion("bb3", "Bir takımda sahada kaç oyuncu bulunur?", listOf("4", "5", "6", "7"), 1, "Basketbol"),
            QuizQuestion("bb4", "Double-double ne demektir?", listOf("İki sayı", "İki istatistikte çift haneli rakam", "İki asist", "İki ribaund"), 1, "Basketbol"),
            QuizQuestion("bb5", "Hücum süresi kaç saniyedir?", listOf("20 sn", "24 sn", "30 sn", "35 sn"), 1, "Basketbol"),
            QuizQuestion("bb6", "Serbest atış kaç sayı değerindedir?", listOf("0.5", "1", "2", "3"), 1, "Basketbol"),
            QuizQuestion("bb7", "Bir oyuncu kaç faulle diskalifiye olur?", listOf("4", "5", "6", "7"), 2, "Basketbol"),
            QuizQuestion("bb8", "Alley-oop nedir?", listOf("Üç sayılık atış", "Havada pas alıp smaç", "Serbest atış", "Ribaund"), 1, "Basketbol"),
            QuizQuestion("bb9", "Top kullanma ihlali hangisidir?", listOf("Çift top sürme", "Top çalma", "Blok", "Asist"), 0, "Basketbol"),
            QuizQuestion("bb10", "Triple-double için kaç farklı istatistikte çift hane gerekir?", listOf("2", "3", "4", "5"), 1, "Basketbol")
        ),
        
        "Fitness" to listOf(
            QuizQuestion("fit1", "Kas gelişimi için ideal protein alımı vücut ağırlığının kilogramı başına kaç gramdır?", listOf("0.5-0.8g", "1.6-2.2g", "3-4g", "0.2-0.4g"), 1, "Fitness"),
            QuizQuestion("fit2", "Compound (bileşik) egzersiz nedir?", listOf("Tek kas grubu çalıştıran", "Birden fazla kas grubu çalıştıran", "Kardiyo egzersizi", "Esneme hareketi"), 1, "Fitness"),
            QuizQuestion("fit3", "Squat hareketi hangi kas gruplarını çalıştırır?", listOf("Sadece bacak", "Bacak, kalça, core", "Sadece karın", "Kol ve omuz"), 1, "Fitness"),
            QuizQuestion("fit4", "HIIT antrenmanı ne demektir?", listOf("Düşük yoğunluklu", "Yüksek yoğunluklu interval antrenman", "Orta tempo kardiyo", "Sadece ağırlık çalışması"), 1, "Fitness"),
            QuizQuestion("fit5", "Progressive overload prensibi nedir?", listOf("Aynı ağırlıkla çalışma", "Zamanla yükü/zorluğu artırma", "Sadece kardiyo yapma", "Hafif ağırlık çok tekrar"), 1, "Fitness"),
            QuizQuestion("fit6", "Deadlift hareketi için temel pozisyon nasıl olmalı?", listOf("Sırt bükük", "Sırt düz, core sıkı", "Dizler kilitli", "Omuzlar öne"), 1, "Fitness"),
            QuizQuestion("fit7", "Rest (dinlenme) süreleri neden önemlidir?", listOf("Sıkılmamak için", "Kas onarımı için", "Sadece nefes almak için", "Önemli değil"), 1, "Fitness"),
            QuizQuestion("fit8", "Bench press hangi kas grubunu hedefler?", listOf("Sırt", "Göğüs", "Bacak", "Karın"), 1, "Fitness"),
            QuizQuestion("fit9", "Isınma (warm-up) neden yapılır?", listOf("Zaman geçirmek için", "Kas ve eklemleri harekete hazırlamak", "Sadece gelenek", "Kilo vermek için"), 1, "Fitness"),
            QuizQuestion("fit10", "Rep (tekrar) ve Set arasındaki fark nedir?", listOf("Aynı şey", "Rep tek hareket, Set tekrar grupları", "Set tek hareket", "Fark yok"), 1, "Fitness")
        ),
        
        // ========== DİLLER ==========
        "İngilizce" to listOf(
            QuizQuestion("en1", "'I have been waiting' hangi zaman yapısıdır?", listOf("Simple Past", "Present Perfect Continuous", "Future Perfect", "Past Continuous"), 1, "İngilizce"),
            QuizQuestion("en2", "'Their', 'There', 'They're' arasındaki fark nedir?", listOf("Hepsi aynı", "Sahiplik, yer, kısaltma", "Sadece yazım farkı", "Sadece telaffuz farkı"), 1, "İngilizce"),
            QuizQuestion("en3", "Passive voice (edilgen çatı) nasıl oluşturulur?", listOf("do + fiil", "be + past participle", "have + fiil", "will + fiil"), 1, "İngilizce"),
            QuizQuestion("en4", "'Although' kelimesinin anlamı nedir?", listOf("Çünkü", "Rağmen/Her ne kadar", "Bu yüzden", "Eğer"), 1, "İngilizce"),
            QuizQuestion("en5", "Countable ve Uncountable noun farkı nedir?", listOf("Sadece çoğul kullanım", "Sayılabilir ve sayılamaz isimler", "Soyut ve somut", "Canlı ve cansız"), 1, "İngilizce"),
            QuizQuestion("en6", "'Would' ne zaman kullanılır?", listOf("Gelecek zaman", "Koşullu cümleler, kibar istekler", "Geçmiş zaman", "Şimdiki zaman"), 1, "İngilizce"),
            QuizQuestion("en7", "Comparative adjective nasıl yapılır?", listOf("-est eki", "-er eki veya more", "-ly eki", "-ing eki"), 1, "İngilizce"),
            QuizQuestion("en8", "'In', 'On', 'At' zaman edatları nasıl kullanılır?", listOf("Hepsi aynı", "In:ay/yıl, On:gün, At:saat", "Sadece yer için", "Rastgele"), 1, "İngilizce"),
            QuizQuestion("en9", "Reported speech (dolaylı anlatım) için ne değişir?", listOf("Hiçbir şey", "Zaman ve zamir", "Sadece soru işareti", "Sadece nokta"), 1, "İngilizce"),
            QuizQuestion("en10", "'A' ve 'An' article farkı nedir?", listOf("A:çoğul, An:tekil", "A:sessiz, An:sesli harf önünde", "Fark yok", "A:soru, An:olumlu"), 1, "İngilizce")
        ),
        
        "Almanca" to listOf(
            QuizQuestion("de1", "Almanca'da kaç tane artikel (tanımlık) vardır?", listOf("2", "3", "4", "5"), 1, "Almanca"),
            QuizQuestion("de2", "'Der', 'Die', 'Das' ne anlama gelir?", listOf("Ben, sen, o", "Eril, dişil, nötr tanımlık", "Bir, iki, üç", "Evet, hayır, belki"), 1, "Almanca"),
            QuizQuestion("de3", "Almanca'da fiiller cümlede nerede bulunur?", listOf("Başta", "Sonda", "İkinci pozisyonda", "Rastgele"), 2, "Almanca"),
            QuizQuestion("de4", "'Ich bin' ne anlama gelir?", listOf("Ben gidiyorum", "Ben varım/im", "Ben istiyorum", "Ben yapıyorum"), 1, "Almanca"),
            QuizQuestion("de5", "Almanca'da kaç hal (Kasus) vardır?", listOf("2", "3", "4", "5"), 2, "Almanca"),
            QuizQuestion("de6", "'Guten Tag' ne demektir?", listOf("Günaydın", "İyi günler", "İyi geceler", "Hoşça kal"), 1, "Almanca"),
            QuizQuestion("de7", "Akkusativ hali ne zaman kullanılır?", listOf("Özne için", "Belirtili nesne için", "Dolaylı nesne için", "Yer bildirmek için"), 1, "Almanca"),
            QuizQuestion("de8", "'Haben' fiilinin 'ich' ile çekimi nedir?", listOf("habe", "hast", "hat", "haben"), 0, "Almanca"),
            QuizQuestion("de9", "Trennbare Verben (ayrılabilen fiiller) nedir?", listOf("İsim fiiller", "Önek ayrılan fiiller", "Düzensiz fiiller", "Yardımcı fiiller"), 1, "Almanca"),
            QuizQuestion("de10", "'Wie heißt du?' ne demektir?", listOf("Nerelisin?", "Adın ne?", "Nasılsın?", "Kaç yaşındasın?"), 1, "Almanca")
        ),
        
        // ========== AKADEMİK ==========
        "Matematik" to listOf(
            QuizQuestion("mat1", "Pi sayısının yaklaşık değeri nedir?", listOf("2.14", "3.14", "4.14", "1.14"), 1, "Matematik"),
            QuizQuestion("mat2", "Bir üçgenin iç açıları toplamı kaç derecedir?", listOf("90°", "180°", "270°", "360°"), 1, "Matematik"),
            QuizQuestion("mat3", "x² - 9 = 0 denkleminin çözüm kümesi nedir?", listOf("{3}", "{-3}", "{3, -3}", "{9}"), 2, "Matematik"),
            QuizQuestion("mat4", "Logaritma tabanı 10 olan log(100) değeri kaçtır?", listOf("1", "2", "10", "100"), 1, "Matematik"),
            QuizQuestion("mat5", "Türev f'(x) neyi gösterir?", listOf("Alan", "Eğim/değişim hızı", "Hacim", "Uzunluk"), 1, "Matematik"),
            QuizQuestion("mat6", "Faktöriyel (5!) değeri kaçtır?", listOf("15", "25", "120", "720"), 2, "Matematik"),
            QuizQuestion("mat7", "İki sayının EBOB'u ne demektir?", listOf("En büyük ortak bölen", "En küçük ortak kat", "Ortalama", "Toplam"), 0, "Matematik"),
            QuizQuestion("mat8", "sin²θ + cos²θ değeri kaçtır?", listOf("0", "1", "2", "θ'ya bağlı"), 1, "Matematik"),
            QuizQuestion("mat9", "Bir karenin köşegen uzunluğu kenarın kaç katıdır?", listOf("1", "√2", "2", "√3"), 1, "Matematik"),
            QuizQuestion("mat10", "Limit kavramı ne için kullanılır?", listOf("Türev hesaplama", "Bir fonksiyonun yaklaşım değeri", "Çarpma işlemi", "Bölme işlemi"), 1, "Matematik")
        ),
        
        "Fizik" to listOf(
            QuizQuestion("fiz1", "Newton'un ikinci hareket yasası formülü nedir?", listOf("F = m/a", "F = m × a", "F = a/m", "F = m + a"), 1, "Fizik"),
            QuizQuestion("fiz2", "Işık hızı yaklaşık kaç m/s'dir?", listOf("3×10⁶", "3×10⁸", "3×10¹⁰", "3×10⁴"), 1, "Fizik"),
            QuizQuestion("fiz3", "Yerçekimi ivmesi (g) yaklaşık kaç m/s²'dir?", listOf("8.9", "9.8", "10.8", "6.8"), 1, "Fizik"),
            QuizQuestion("fiz4", "Ohm Yasası formülü nedir?", listOf("V = I × R", "V = I / R", "V = I + R", "V = I - R"), 0, "Fizik"),
            QuizQuestion("fiz5", "Kinetik enerji formülü nedir?", listOf("E = mc²", "E = ½mv²", "E = mgh", "E = Fd"), 1, "Fizik"),
            QuizQuestion("fiz6", "Frekans birimi nedir?", listOf("Metre", "Saniye", "Hertz", "Joule"), 2, "Fizik"),
            QuizQuestion("fiz7", "Ses boşlukta yayılır mı?", listOf("Evet", "Hayır", "Bazen", "Sadece yüksek seste"), 1, "Fizik"),
            QuizQuestion("fiz8", "1 atmosfer basınç kaç Pascal'dır?", listOf("101.325", "1013.25", "10132.5", "101325"), 3, "Fizik"),
            QuizQuestion("fiz9", "Momentum formülü nedir?", listOf("p = m/v", "p = m × v", "p = v/m", "p = m + v"), 1, "Fizik"),
            QuizQuestion("fiz10", "Termodinamiğin birinci yasası neyi ifade eder?", listOf("Entropinin artışı", "Enerjinin korunumu", "Sıfır mutlak sıcaklık", "Isı akışı"), 1, "Fizik")
        ),
        
        // ========== SANAT ==========
        "Fotoğrafçılık" to listOf(
            QuizQuestion("foto1", "ISO değeri ne kontrol eder?", listOf("Odak", "Işık hassasiyeti", "Renk sıcaklığı", "Zoom"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto2", "Diyafram (aperture) ne kontrol eder?", listOf("Poz süresi", "Işık miktarı ve alan derinliği", "ISO", "Beyaz dengesi"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto3", "Düşük f-stop değeri (f/1.8) ne anlama gelir?", listOf("Dar açıklık", "Geniş açıklık, bulanık arka plan", "Uzun poz", "Yüksek ISO"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto4", "Üçler kuralı (Rule of thirds) nedir?", listOf("3 fotoğraf çekme", "Kadrajı 9 parçaya bölme kompozisyon tekniği", "3 kişi fotoğrafı", "3 renk kullanma"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto5", "RAW format neden tercih edilir?", listOf("Küçük dosya boyutu", "Daha fazla düzenleme esnekliği", "Hızlı paylaşım", "Otomatik düzeltme"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto6", "Shutter speed (enstantane) ne kontrol eder?", listOf("Işık hassasiyeti", "Poz süresi/hareket bulanıklığı", "Renk", "Odak"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto7", "Golden hour ne zaman olur?", listOf("Öğlen", "Gün doğumu/batımı", "Gece", "Bulutlu hava"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto8", "Bokeh efekti nasıl elde edilir?", listOf("Yüksek ISO", "Geniş diyafram (düşük f-stop)", "Uzun poz", "Dar diyafram"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto9", "White balance (beyaz dengesi) ne ayarlar?", listOf("Parlaklık", "Renk sıcaklığı", "Kontrast", "Keskinlik"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto10", "Exposure triangle'ın üç unsuru nedir?", listOf("RGB", "ISO, Aperture, Shutter Speed", "Brightness, Contrast, Saturation", "Hue, Saturation, Lightness"), 1, "Fotoğrafçılık")
        ),
        
        "Grafik Tasarım" to listOf(
            QuizQuestion("gt1", "RGB renk modeli ne için kullanılır?", listOf("Baskı", "Ekran/dijital", "Tekstil", "Tümü"), 1, "Grafik Tasarım"),
            QuizQuestion("gt2", "CMYK renk modeli ne için kullanılır?", listOf("Dijital", "Baskı/matbaa", "Web", "Video"), 1, "Grafik Tasarım"),
            QuizQuestion("gt3", "Vector grafikler neden tercih edilir?", listOf("Küçük dosya", "Kalite kaybı olmadan ölçekleme", "Fotoğraf için", "Hızlı yükleme"), 1, "Grafik Tasarım"),
            QuizQuestion("gt4", "DPI ne anlama gelir?", listOf("Digital Print Image", "Dots Per Inch", "Design Process Index", "Display Pixel Interface"), 1, "Grafik Tasarım"),
            QuizQuestion("gt5", "Baskı için ideal çözünürlük kaç DPI olmalıdır?", listOf("72 DPI", "150 DPI", "300 DPI", "50 DPI"), 2, "Grafik Tasarım"),
            QuizQuestion("gt6", "Kerning ne demektir?", listOf("Satır aralığı", "Harf aralığı ayarlama", "Paragraf girintisi", "Font boyutu"), 1, "Grafik Tasarım"),
            QuizQuestion("gt7", "Complementary colors (tamamlayıcı renkler) nedir?", listOf("Aynı renkler", "Renk çemberinde karşılıklı renkler", "Siyah ve beyaz", "Primer renkler"), 1, "Grafik Tasarım"),
            QuizQuestion("gt8", "Negative space ne demektir?", listOf("Siyah alan", "Tasarımdaki boş alan", "Negatif renk", "Silinmiş alan"), 1, "Grafik Tasarım"),
            QuizQuestion("gt9", "Sans-serif font özelliği nedir?", listOf("Süslü uçlar var", "Süslü uçlar yok/düz", "El yazısı", "Gotik"), 1, "Grafik Tasarım"),
            QuizQuestion("gt10", "Bleed (taşma payı) neden bırakılır?", listOf("Estetik için", "Baskı kesim toleransı için", "Dijital için", "Renk için"), 1, "Grafik Tasarım")
        ),
        
        // ========== EK YAZILIM DİLLERİ ==========
        "TypeScript" to listOf(
            QuizQuestion("ts1", "TypeScript, JavaScript'in neyi ekler?", listOf("Hız", "Statik tip sistemi", "Veritabanı", "Grafik"), 1, "TypeScript"),
            QuizQuestion("ts2", "TypeScript dosyalarının uzantısı nedir?", listOf(".js", ".ts", ".tsx", ".tsc"), 1, "TypeScript"),
            QuizQuestion("ts3", "TypeScript'te 'interface' ne için kullanılır?", listOf("Fonksiyon tanımı", "Tip şablonu tanımlama", "Değişken", "Döngü"), 1, "TypeScript"),
            QuizQuestion("ts4", "'any' tipi ne anlama gelir?", listOf("Sayı", "Herhangi bir tip", "Dizi", "Nesne"), 1, "TypeScript"),
            QuizQuestion("ts5", "TypeScript'te 'enum' ne için kullanılır?", listOf("Döngü", "Sabit değerler kümesi", "Fonksiyon", "Sınıf"), 1, "TypeScript"),
            QuizQuestion("ts6", "'readonly' modifier ne yapar?", listOf("Değeri siler", "Değeri değiştirilemez yapar", "Değeri kopyalar", "Değeri yazdırır"), 1, "TypeScript"),
            QuizQuestion("ts7", "TypeScript'te union type nasıl yazılır?", listOf("type A + B", "type A | B", "type A & B", "type A - B"), 1, "TypeScript"),
            QuizQuestion("ts8", "'unknown' ve 'any' arasındaki fark nedir?", listOf("Aynı", "unknown daha güvenli", "any daha güvenli", "Fark yok"), 1, "TypeScript"),
            QuizQuestion("ts9", "TypeScript'te generic nasıl tanımlanır?", listOf("generic<T>", "<T>", "[T]", "{T}"), 1, "TypeScript"),
            QuizQuestion("ts10", "TypeScript kodunu JavaScript'e çeviren araç hangisidir?", listOf("Babel", "tsc (TypeScript Compiler)", "Webpack", "Node"), 1, "TypeScript")
        ),
        
        "C++" to listOf(
            QuizQuestion("cpp1", "C++'da pointer tanımlamak için hangi sembol kullanılır?", listOf("&", "*", "@", "#"), 1, "C++"),
            QuizQuestion("cpp2", "C++'da 'cout' ne için kullanılır?", listOf("Giriş", "Çıkış/yazdırma", "Dosya okuma", "Hesaplama"), 1, "C++"),
            QuizQuestion("cpp3", "C++'da header dosyalarının uzantısı nedir?", listOf(".cpp", ".h veya .hpp", ".c", ".hd"), 1, "C++"),
            QuizQuestion("cpp4", "C++'da 'new' operatörü ne yapar?", listOf("Değişken siler", "Dinamik bellek ayırır", "Fonksiyon çağırır", "Döngü başlatır"), 1, "C++"),
            QuizQuestion("cpp5", "C++'da referans operatörü hangisidir?", listOf("*", "&", "@", "^"), 1, "C++"),
            QuizQuestion("cpp6", "'#include' direktifi ne yapar?", listOf("Değişken tanımlar", "Dosya içeriğini ekler", "Fonksiyon çağırır", "Yorum ekler"), 1, "C++"),
            QuizQuestion("cpp7", "C++'da 'virtual' anahtar kelimesi ne için kullanılır?", listOf("Sanal bellek", "Polimorfizm/geçersiz kılma", "Değişken tipi", "Döngü"), 1, "C++"),
            QuizQuestion("cpp8", "C++'da destructor nasıl tanımlanır?", listOf("~ClassName()", "!ClassName()", "-ClassName()", "delete ClassName()"), 0, "C++"),
            QuizQuestion("cpp9", "C++'da 'nullptr' ne demektir?", listOf("Sıfır değeri", "Boş pointer", "Hata", "Sonsuz"), 1, "C++"),
            QuizQuestion("cpp10", "STL ne anlama gelir?", listOf("Simple Type Library", "Standard Template Library", "Static Type Language", "String Template List"), 1, "C++")
        ),
        
        "C#" to listOf(
            QuizQuestion("cs1", "C#'ta namespace ne için kullanılır?", listOf("Değişken tanımı", "Kod organizasyonu", "Döngü", "Fonksiyon"), 1, "C#"),
            QuizQuestion("cs2", "C#'ta 'var' anahtar kelimesi ne yapar?", listOf("Değişken siler", "Tip çıkarımı yapar", "Sabit tanımlar", "Hata yakalar"), 1, "C#"),
            QuizQuestion("cs3", "C#'ta property tanımlamak için ne kullanılır?", listOf("prop", "get/set", "property", "define"), 1, "C#"),
            QuizQuestion("cs4", "C#'ta 'async' ve 'await' ne için kullanılır?", listOf("Senkron işlem", "Asenkron programlama", "Hata yakalama", "Döngü"), 1, "C#"),
            QuizQuestion("cs5", "LINQ ne anlama gelir?", listOf("Linux Query", "Language Integrated Query", "List Input Queue", "Linked Items Queue"), 1, "C#"),
            QuizQuestion("cs6", "C#'ta 'delegate' ne demektir?", listOf("Sınıf", "Metod referansı", "Değişken", "Interface"), 1, "C#"),
            QuizQuestion("cs7", "C#'ta null kontrolü için '??' operatörü ne yapar?", listOf("Karşılaştırma", "Null ise alternatif değer", "Toplama", "Çıkarma"), 1, "C#"),
            QuizQuestion("cs8", "C#'ta 'sealed' sınıf ne demektir?", listOf("Kalıtıma kapalı", "Soyut", "Statik", "Genel"), 0, "C#"),
            QuizQuestion("cs9", "C#'ta 'using' statement ne sağlar?", listOf("Import", "Kaynak yönetimi (IDisposable)", "Döngü", "Koşul"), 1, "C#"),
            QuizQuestion("cs10", ".NET Framework'ün temel dili hangisidir?", listOf("Java", "C#", "Python", "C++"), 1, "C#")
        ),
        
        "Swift" to listOf(
            QuizQuestion("sw1", "Swift hangi şirket tarafından geliştirildi?", listOf("Google", "Microsoft", "Apple", "Meta"), 2, "Swift"),
            QuizQuestion("sw2", "Swift'te optional değişken nasıl tanımlanır?", listOf("String!", "String?", "String*", "?String"), 1, "Swift"),
            QuizQuestion("sw3", "Swift'te sabit tanımlamak için hangi anahtar kelime kullanılır?", listOf("var", "let", "const", "final"), 1, "Swift"),
            QuizQuestion("sw4", "Swift'te 'guard' statement ne için kullanılır?", listOf("Döngü", "Erken çıkış kontrolü", "Fonksiyon tanımı", "Sınıf tanımı"), 1, "Swift"),
            QuizQuestion("sw5", "Swift'te 'struct' ve 'class' arasındaki temel fark nedir?", listOf("İsim", "Value vs Reference type", "Hız", "Boyut"), 1, "Swift"),
            QuizQuestion("sw6", "Swift'te string interpolation nasıl yapılır?", listOf("\"{var}\"", "\"\\(var)\"", "\"#{var}\"", "\"\$var\""), 1, "Swift"),
            QuizQuestion("sw7", "Swift'te 'protocol' ne demektir?", listOf("Sınıf", "Interface benzeri yapı", "Değişken", "Döngü"), 1, "Swift"),
            QuizQuestion("sw8", "Swift'te 'extension' ne sağlar?", listOf("Dosya uzantısı", "Mevcut tipe özellik ekleme", "Kalıtım", "Import"), 1, "Swift"),
            QuizQuestion("sw9", "Swift'te 'nil' ne demektir?", listOf("Sıfır", "Değer yok", "Hata", "Boş string"), 1, "Swift"),
            QuizQuestion("sw10", "SwiftUI ne için kullanılır?", listOf("Backend", "Deklaratif UI framework", "Database", "Network"), 1, "Swift")
        ),
        
        "HTML" to listOf(
            QuizQuestion("html1", "HTML ne anlama gelir?", listOf("Hyper Text Markup Language", "High Tech Modern Language", "Home Tool Markup Language", "Hyperlink Text Mode Language"), 0, "HTML"),
            QuizQuestion("html2", "HTML'de başlık etiketi hangisidir?", listOf("<header>", "<h1>", "<title>", "<head>"), 1, "HTML"),
            QuizQuestion("html3", "HTML'de link oluşturmak için hangi etiket kullanılır?", listOf("<link>", "<a>", "<href>", "<url>"), 1, "HTML"),
            QuizQuestion("html4", "HTML'de resim eklemek için hangi etiket kullanılır?", listOf("<picture>", "<img>", "<image>", "<photo>"), 1, "HTML"),
            QuizQuestion("html5", "HTML'de sırasız liste etiketi hangisidir?", listOf("<ol>", "<ul>", "<li>", "<list>"), 1, "HTML"),
            QuizQuestion("html6", "HTML5'te video eklemek için hangi etiket kullanılır?", listOf("<media>", "<video>", "<movie>", "<film>"), 1, "HTML"),
            QuizQuestion("html7", "HTML'de form gönderme butonu nasıl oluşturulur?", listOf("<button>", "<submit>", "<input type='submit'>", "<send>"), 2, "HTML"),
            QuizQuestion("html8", "HTML'de yorum satırı nasıl yazılır?", listOf("// yorum", "/* yorum */", "<!-- yorum -->", "# yorum"), 2, "HTML"),
            QuizQuestion("html9", "HTML'de tablo satırı etiketi hangisidir?", listOf("<td>", "<tr>", "<th>", "<table>"), 1, "HTML"),
            QuizQuestion("html10", "HTML'de 'div' etiketi ne için kullanılır?", listOf("Görsel", "Bölüm/container", "Link", "Liste"), 1, "HTML")
        ),
        
        "CSS" to listOf(
            QuizQuestion("css1", "CSS ne anlama gelir?", listOf("Computer Style Sheets", "Cascading Style Sheets", "Creative Style System", "Colorful Style Sheets"), 1, "CSS"),
            QuizQuestion("css2", "CSS'te class seçici nasıl yazılır?", listOf("#class", ".class", "*class", "@class"), 1, "CSS"),
            QuizQuestion("css3", "CSS'te ID seçici nasıl yazılır?", listOf(".id", "#id", "*id", "@id"), 1, "CSS"),
            QuizQuestion("css4", "CSS'te Flexbox'ı etkinleştirmek için hangi özellik kullanılır?", listOf("display: flex", "flex: true", "flexbox: on", "layout: flex"), 0, "CSS"),
            QuizQuestion("css5", "CSS'te margin ne demektir?", listOf("İç boşluk", "Dış boşluk", "Kenarlık", "Arka plan"), 1, "CSS"),
            QuizQuestion("css6", "CSS'te padding ne demektir?", listOf("Dış boşluk", "İç boşluk", "Kenarlık", "Gölge"), 1, "CSS"),
            QuizQuestion("css7", "CSS'te 'position: absolute' ne yapar?", listOf("Göreli konumlandırma", "Mutlak konumlandırma", "Sabit konumlandırma", "Normal akış"), 1, "CSS"),
            QuizQuestion("css8", "CSS'te ':hover' ne zaman tetiklenir?", listOf("Tıklandığında", "Fare üzerine gelince", "Sayfa yüklenince", "Focus olunca"), 1, "CSS"),
            QuizQuestion("css9", "CSS Grid'i etkinleştirmek için hangi özellik kullanılır?", listOf("display: grid", "grid: true", "layout: grid", "type: grid"), 0, "CSS"),
            QuizQuestion("css10", "CSS'te 'z-index' ne kontrol eder?", listOf("Genişlik", "Yükseklik", "Katman sırası", "Opaklık"), 2, "CSS")
        ),
        
        "Node.js" to listOf(
            QuizQuestion("node1", "Node.js hangi JavaScript motorunu kullanır?", listOf("SpiderMonkey", "V8", "Chakra", "JavaScriptCore"), 1, "Node.js"),
            QuizQuestion("node2", "Node.js'te paket yöneticisi hangisidir?", listOf("pip", "npm", "gem", "maven"), 1, "Node.js"),
            QuizQuestion("node3", "Node.js'te dosya okumak için hangi modül kullanılır?", listOf("http", "fs", "path", "os"), 1, "Node.js"),
            QuizQuestion("node4", "package.json dosyası ne içerir?", listOf("Kaynak kodu", "Proje bağımlılıkları ve metadata", "Derleme çıktısı", "Test sonuçları"), 1, "Node.js"),
            QuizQuestion("node5", "Node.js'te 'require' ne yapar?", listOf("Değişken tanımlar", "Modül içe aktarır", "Fonksiyon çağırır", "Dosya siler"), 1, "Node.js"),
            QuizQuestion("node6", "Express.js ne için kullanılır?", listOf("Database", "Web framework", "Test", "Build"), 1, "Node.js"),
            QuizQuestion("node7", "Node.js'te asenkron işlem için hangisi kullanılır?", listOf("sync()", "callback/Promise/async-await", "thread()", "parallel()"), 1, "Node.js"),
            QuizQuestion("node8", "'npm install' komutu ne yapar?", listOf("Node'u yükler", "Bağımlılıkları yükler", "Projeyi çalıştırır", "Test çalıştırır"), 1, "Node.js"),
            QuizQuestion("node9", "Node.js'te HTTP sunucu oluşturmak için hangi modül kullanılır?", listOf("fs", "http", "url", "net"), 1, "Node.js"),
            QuizQuestion("node10", "Node.js single-threaded mı multi-threaded mı?", listOf("Multi-threaded", "Single-threaded (event loop)", "Her ikisi de değil", "Duruma göre değişir"), 1, "Node.js")
        ),

        "Dart" to listOf(
            QuizQuestion("dart1", "Dart'ta değişmez değişken için hangi anahtar kelime kullanılır?", listOf("var", "let", "final", "const"), 2, "Dart"),
            QuizQuestion("dart2", "Dart'ta 'null safety' ne sağlar?", listOf("Hız", "Null referans hatalarını önleme", "Bellek", "Renk"), 1, "Dart"),
            QuizQuestion("dart3", "Dart'ta liste nasıl tanımlanır?", listOf("list()", "List()", "array()", "[] sadece"), 1, "Dart"),
            QuizQuestion("dart4", "Dart'ta async fonksiyon nasıl tanımlanır?", listOf("function async", "async function", "async/fn", "async anahtar kelimesiyle"), 3, "Dart"),
            QuizQuestion("dart5", "Dart'ta future beklemek için hangi anahtar kelime kullanılır?", listOf("then", "await", "wait", "delay"), 1, "Dart"),
            QuizQuestion("dart6", "Dart'ta map literal nasıl yazılır?", listOf("[]", "()", "{}", "<>"), 2, "Dart"),
            QuizQuestion("dart7", "Dart'ta 'late' anahtar kelimesi ne yapar?", listOf("Erken değer atar", "Geç init, null safety uyumlu", "Silme yapar", "Gizler"), 1, "Dart"),
            QuizQuestion("dart8", "Dart'ta mixin ne için kullanılır?", listOf("Kalıtım", "Kod paylaşımı / davranış ekleme", "UI", "Network"), 1, "Dart"),
            QuizQuestion("dart9", "Dart'ta 'const' ile 'final' farkı nedir?", listOf("Aynı", "const derleme zamanı sabit, final çalışma zamanı tek atama", "const değişken, final sabit", "Fark yok"), 1, "Dart"),
            QuizQuestion("dart10", "Dart hangi amaçla tasarlandı?", listOf("Sistem dili", "Web/Flutter için modern dil", "Veritabanı", "Oyun motoru"), 1, "Dart")
        ),

        "Go" to listOf(
            QuizQuestion("go1", "Go dilinde paket bildirim satırı nasıl yazılır?", listOf("package main", "import main", "module main", "pkg main"), 0, "Go"),
            QuizQuestion("go2", "Go'da değişken tanımlamak için kısa yol hangisidir?", listOf("var x int", "let x int", "x := 5", "x = int"), 2, "Go"),
            QuizQuestion("go3", "Go'da concurrency için kullanılan temel yapı nedir?", listOf("threads", "goroutine", "process", "task"), 1, "Go"),
            QuizQuestion("go4", "Goroutine'ler arası iletişim için ne kullanılır?", listOf("locks", "channels", "semaphores", "queues"), 1, "Go"),
            QuizQuestion("go5", "Go'da hata nasıl temsil edilir?", listOf("exception", "panic", "error interface değeri", "throw"), 2, "Go"),
            QuizQuestion("go6", "Go'da interface nedir?", listOf("Soyut sınıf", "Metod imzaları tanımı", "Struct", "Enum"), 1, "Go"),
            QuizQuestion("go7", "Go'da map literal nasıl yazılır?", listOf("map()", "{}", "map[string]int{}", "make(map)"), 2, "Go"),
            QuizQuestion("go8", "Go modül yönetim dosyası hangisidir?", listOf("package.json", "go.mod", "pom.xml", "build.gradle"), 1, "Go"),
            QuizQuestion("go9", "Go'da defer ne yapar?", listOf("İptal", "Fonksiyonu hemen çalıştırır", "Fonksiyonu kapsam bitiminde çalıştırır", "Thread başlatır"), 2, "Go"),
            QuizQuestion("go10", "Go dilini kim geliştirdi?", listOf("Microsoft", "Apple", "Google", "IBM"), 2, "Go")
        ),

        "Frontend" to listOf(
            QuizQuestion("fe1", "SPA ne anlama gelir?", listOf("Single Page Application", "Simple Program App", "Server Page App", "Static Page App"), 0, "Frontend"),
            QuizQuestion("fe2", "Bundle boyutunu küçültmek için ne kullanılır?", listOf("Minification/treeshaking", "Daha çok JS eklemek", "Daha büyük resim", "Inline CSS"), 0, "Frontend"),
            QuizQuestion("fe3", "Responsive tasarım için hangi teknik kullanılır?", listOf("Sabit px", "Media queries, flex/grid", "Tablo layout", "Sadece JS"), 1, "Frontend"),
            QuizQuestion("fe4", "CSR ve SSR farkı nedir?", listOf("Aynı", "CSR:tarayıcı render, SSR:server render", "SSR hızlı değildir", "CSR sunucuda çalışır"), 1, "Frontend"),
            QuizQuestion("fe5", "Webpack/Vite ne yapar?", listOf("Veritabanı", "Bundler/build aracı", "Test runner", "Sunucu"), 1, "Frontend"),
            QuizQuestion("fe6", "Accessibility (a11y) neden önemlidir?", listOf("Önemli değil", "Erişilebilirlik ve kapsayıcılık", "Sadece SEO", "Sadece hız"), 1, "Frontend"),
            QuizQuestion("fe7", "Lighthouse hangi amaçla kullanılır?", listOf("Debug", "Performans/SEO/a11y ölçümü", "Deploy", "Veritabanı"), 1, "Frontend"),
            QuizQuestion("fe8", "HTTP caching için hangi header kullanılır?", listOf("Content-Type", "Cache-Control", "Accept", "Host"), 1, "Frontend"),
            QuizQuestion("fe9", "Service Worker ne sağlar?", listOf("UI animasyonu", "Offline/önbellek ve background sync", "CSS derleme", "Veritabanı"), 1, "Frontend"),
            QuizQuestion("fe10", "Critical CSS ne için yapılır?", listOf("Daha çok JS", "Above-the-fold hızını artırmak", "SEO gizlemek", "Resim sıkıştırmak"), 1, "Frontend")
        ),

        "Backend" to listOf(
            QuizQuestion("be1", "REST nedir?", listOf("Veritabanı", "HTTP tabanlı mimari stil", "Önbellek", "Mesaj kuyruğu"), 1, "Backend"),
            QuizQuestion("be2", "HTTP 201 status neyi ifade eder?", listOf("Başarısız", "Oluşturuldu", "Yetkisiz", "Sunucu hatası"), 1, "Backend"),
            QuizQuestion("be3", "JWT ne için kullanılır?", listOf("Dosya depolama", "Token tabanlı kimlik doğrulama", "Önbellek", "Loglama"), 1, "Backend"),
            QuizQuestion("be4", "ORM ne işe yarar?", listOf("Veri tablosu çizme", "Veritabanı nesne eşleme", "Önbellek", "Queue"), 1, "Backend"),
            QuizQuestion("be5", "ACID içinde 'I' neyi temsil eder?", listOf("Isolation", "Integration", "Iteration", "Index"), 0, "Backend"),
            QuizQuestion("be6", "Cache katmanı ne sağlar?", listOf("Yavaşlatır", "Okuma hızını artırır", "Disk ekler", "Şifreler"), 1, "Backend"),
            QuizQuestion("be7", "Rate limiting neden yapılır?", listOf("Daha çok trafik", "İstismar/DoS önlemek", "Veri gizlemek", "SEO"), 1, "Backend"),
            QuizQuestion("be8", "Idempotent HTTP method hangisidir?", listOf("POST", "PUT/DELETE/GET", "PATCH", "CONNECT"), 1, "Backend"),
            QuizQuestion("be9", "Message queue (RabbitMQ/Kafka) ne için kullanılır?", listOf("Önbellek", "Asenkron iş kuyruğu", "Database", "Log"), 1, "Backend"),
            QuizQuestion("be10", "Twelve-Factor App hangi tür uygulamalar içindir?", listOf("Masaüstü", "Bulut-native SaaS", "Donanım", "Oyun"), 1, "Backend")
        ),

        "Fullstack" to listOf(
            QuizQuestion("fs1", "Fullstack geliştirici ne yapar?", listOf("Sadece frontend", "Frontend + Backend", "Sadece mobil", "Sadece veri"), 1, "Fullstack"),
            QuizQuestion("fs2", "API Gateway rolü nedir?", listOf("Veritabanı", "Tek giriş noktası, auth/rate limit", "Önbellek", "Depolama"), 1, "Fullstack"),
            QuizQuestion("fs3", "CI/CD ne sağlar?", listOf("Elle deploy", "Otomatik build/test/deploy", "Sadece debug", "Sadece log"), 1, "Fullstack"),
            QuizQuestion("fs4", "MVC'de Controller ne yapar?", listOf("Veritabanı", "İstekleri işler, Model ve View'u bağlar", "CSS", "Deployment"), 1, "Fullstack"),
            QuizQuestion("fs5", "GraphQL'in REST'e göre avantajı nedir?", listOf("Daha yavaş", "Tek endpoint ve seçilebilir alanlar", "HTTP kullanmaz", "SQL gerektirir"), 1, "Fullstack"),
            QuizQuestion("fs6", "Docker ne sağlar?", listOf("Sanallaştırma donanımı", "Container ile taşınabilirlik", "IDE", "Veritabanı"), 1, "Fullstack"),
            QuizQuestion("fs7", "Env değişkenleri neden önemlidir?", listOf("Önemsiz", "Konfigürasyon ayrımı (secret, environment)", "Performans", "SEO"), 1, "Fullstack"),
            QuizQuestion("fs8", "Monolit ve mikroservis farkı nedir?", listOf("Aynı", "Monolit tek codebase, mikroservis küçük servisler", "Her ikisi de mikro", "Sadece frontend"), 1, "Fullstack"),
            QuizQuestion("fs9", "Authentication ve Authorization farkı?", listOf("Aynı", "Kimlik doğrulama vs yetkilendirme", "Şifreleme", "Cache"), 1, "Fullstack"),
            QuizQuestion("fs10", "Load balancer ne yapar?", listOf("Hızı düşürür", "Trafiği birden çok sunucuya dağıtır", "Log siler", "Veritabanı kurar"), 1, "Fullstack")
        ),
        
        // ========== EK MOBİL GELİŞTİRME ==========
        "iOS" to listOf(
            QuizQuestion("ios1", "iOS uygulamaları hangi dilde yazılır?", listOf("Java", "Swift/Objective-C", "Kotlin", "C#"), 1, "iOS"),
            QuizQuestion("ios2", "iOS geliştirme için hangi IDE kullanılır?", listOf("Android Studio", "Xcode", "Visual Studio", "Eclipse"), 1, "iOS"),
            QuizQuestion("ios3", "iOS'ta UI oluşturmak için hangi framework kullanılır?", listOf("Android XML", "UIKit/SwiftUI", "WPF", "Qt"), 1, "iOS"),
            QuizQuestion("ios4", "iOS'ta uygulama yaşam döngüsünü yöneten sınıf hangisidir?", listOf("Activity", "AppDelegate", "Application", "Main"), 1, "iOS"),
            QuizQuestion("ios5", "iOS'ta view controller'lar arası geçiş nasıl yapılır?", listOf("Intent", "Segue", "Navigation", "Route"), 1, "iOS"),
            QuizQuestion("ios6", "iOS'ta Auto Layout ne için kullanılır?", listOf("Otomatik kod", "Responsive UI tasarımı", "Veritabanı", "Network"), 1, "iOS"),
            QuizQuestion("ios7", "iOS uygulamaları hangi mağazadan dağıtılır?", listOf("Google Play", "App Store", "Amazon Store", "Microsoft Store"), 1, "iOS"),
            QuizQuestion("ios8", "iOS'ta Core Data ne için kullanılır?", listOf("Network", "Veri persistance", "UI", "Animation"), 1, "iOS"),
            QuizQuestion("ios9", "iOS'ta push notification için hangi servis kullanılır?", listOf("FCM", "APNs", "GCM", "SNS"), 1, "iOS"),
            QuizQuestion("ios10", "TestFlight ne için kullanılır?", listOf("Unit test", "Beta test dağıtımı", "UI test", "Performance test"), 1, "iOS")
        ),
        
        "React Native" to listOf(
            QuizQuestion("rn1", "React Native hangi dili kullanır?", listOf("Java", "Swift", "JavaScript/TypeScript", "Dart"), 2, "React Native"),
            QuizQuestion("rn2", "React Native ile hangi platformlar için uygulama yazılabilir?", listOf("Sadece iOS", "Sadece Android", "iOS ve Android", "Web"), 2, "React Native"),
            QuizQuestion("rn3", "React Native'de native modül nedir?", listOf("JavaScript kodu", "Platform spesifik kod", "CSS stili", "HTML elementi"), 1, "React Native"),
            QuizQuestion("rn4", "React Native'de stil vermek için ne kullanılır?", listOf("CSS dosyası", "StyleSheet API", "SASS", "Less"), 1, "React Native"),
            QuizQuestion("rn5", "React Native CLI alternatifi hangisidir?", listOf("Flutter", "Expo", "Cordova", "Ionic"), 1, "React Native"),
            QuizQuestion("rn6", "React Native'de 'View' komponenti neye karşılık gelir?", listOf("img", "div/container", "span", "button"), 1, "React Native"),
            QuizQuestion("rn7", "React Native'de liste göstermek için hangi komponent kullanılır?", listOf("ListView", "FlatList", "ScrollView", "List"), 1, "React Native"),
            QuizQuestion("rn8", "React Native'de navigation için popüler kütüphane hangisidir?", listOf("React Router", "React Navigation", "Vue Router", "Navigator"), 1, "React Native"),
            QuizQuestion("rn9", "React Native'de 'bridge' ne demektir?", listOf("Köprü UI", "JS ve Native kod arası iletişim", "Network", "Database"), 1, "React Native"),
            QuizQuestion("rn10", "React Native uygulaması nasıl derlenir?", listOf("Tamamen JS", "Native kod + JS bundle", "Sadece native", "Web teknolojileri"), 1, "React Native")
        ),
        
        // ========== EK MÜZİK ==========
        "Keman" to listOf(
            QuizQuestion("km1", "Kemanın kaç teli vardır?", listOf("3", "4", "5", "6"), 1, "Keman"),
            QuizQuestion("km2", "Kemanın telleri (kalından inceye) hangi notalardır?", listOf("E-A-D-G", "G-D-A-E", "A-D-G-B", "D-A-E-B"), 1, "Keman"),
            QuizQuestion("km3", "Keman yayı hangi malzemeden yapılır?", listOf("Metal", "At kılı", "Naylon", "Pamuk"), 1, "Keman"),
            QuizQuestion("km4", "Vibrato tekniği nasıl yapılır?", listOf("Yayı hızlı hareket ettirme", "Parmağı ileri geri sallama", "Teli çekme", "Yayı döndürme"), 1, "Keman"),
            QuizQuestion("km5", "Pizzicato ne demektir?", listOf("Yay ile çalma", "Parmakla tel çekme", "Hızlı çalma", "Yavaş çalma"), 1, "Keman"),
            QuizQuestion("km6", "Kemanın gövdesi genellikle hangi ağaçtan yapılır?", listOf("Meşe", "Akçaağaç ve ladin", "Ceviz", "Çam"), 1, "Keman"),
            QuizQuestion("km7", "Spiccato tekniği nedir?", listOf("Legato çalma", "Yayın zıplaması ile kısa notalar", "Tremolo", "Glissando"), 1, "Keman"),
            QuizQuestion("km8", "Kemanın en ince teli hangi notadır?", listOf("G", "D", "A", "E"), 3, "Keman"),
            QuizQuestion("km9", "Kemanı akort etmek için genellikle hangi nota referans alınır?", listOf("Do", "La (A=440Hz)", "Sol", "Mi"), 1, "Keman"),
            QuizQuestion("km10", "Stradivarius ne ile ünlüdür?", listOf("Piyano yapımı", "Efsanevi keman yapımcısı", "Besteci", "Şef"), 1, "Keman")
        ),
        
        "Bateri" to listOf(
            QuizQuestion("bat1", "Standart davul setinde kaç parça bulunur?", listOf("3", "5", "7", "10"), 1, "Bateri"),
            QuizQuestion("bat2", "Hi-hat nedir?", listOf("Bas davul", "İki zil kombinasyonu", "Tom-tom", "Trampet"), 1, "Bateri"),
            QuizQuestion("bat3", "Snare drum'ın ayırt edici özelliği nedir?", listOf("Büyüklüğü", "Alt taraftaki teller", "Rengi", "Malzemesi"), 1, "Bateri"),
            QuizQuestion("bat4", "Bas davul nasıl çalınır?", listOf("Elle", "Pedal ile", "Baget ile", "Fırça ile"), 1, "Bateri"),
            QuizQuestion("bat5", "Rudiment ne demektir?", listOf("Davul parçası", "Temel vuruş kalıpları", "Zil türü", "Baget tipi"), 1, "Bateri"),
            QuizQuestion("bat6", "Ghost note nedir?", listOf("Sessiz nota", "Hafif/kısık vuruş", "Hızlı vuruş", "Yüksek sesli vuruş"), 1, "Bateri"),
            QuizQuestion("bat7", "Paradiddle ritmi nasıldır?", listOf("R-R-L-L", "R-L-R-R-L-R-L-L", "R-L-R-L", "R-R-R-L"), 1, "Bateri"),
            QuizQuestion("bat8", "Ride cymbal ne için kullanılır?", listOf("Vurgu", "Sürekli ritim tutma", "Fill", "Solo"), 1, "Bateri"),
            QuizQuestion("bat9", "Crash cymbal ne zaman kullanılır?", listOf("Sürekli", "Vurgu ve geçişlerde", "Sadece başlangıçta", "Sadece sonda"), 1, "Bateri"),
            QuizQuestion("bat10", "Metronom ne için kullanılır?", listOf("Ses yükseltme", "Tempo tutma", "Akort", "Kayıt"), 1, "Bateri")
        ),
        
        "Şan" to listOf(
            QuizQuestion("san1", "Diyafram nefesi ne demektir?", listOf("Göğüs nefesi", "Karın/diyafram ile derin nefes", "Burun nefesi", "Kısa nefes"), 1, "Şan"),
            QuizQuestion("san2", "Soprano hangi ses aralığıdır?", listOf("En kalın kadın sesi", "En ince kadın sesi", "Erkek sesi", "Çocuk sesi"), 1, "Şan"),
            QuizQuestion("san3", "Tenor hangi ses aralığıdır?", listOf("Kalın kadın sesi", "İnce erkek sesi", "Kalın erkek sesi", "Çocuk sesi"), 1, "Şan"),
            QuizQuestion("san4", "Vibrato nedir?", listOf("Düz ses", "Sesin hafif titremesi", "Fısıltı", "Bağırma"), 1, "Şan"),
            QuizQuestion("san5", "Falsetto ne demektir?", listOf("Göğüs sesi", "Kafa sesi/ince erkek sesi", "Kalın ses", "Fısıltı"), 1, "Şan"),
            QuizQuestion("san6", "Vokal ısınma egzersizleri neden yapılır?", listOf("Ses tellerini yormak", "Ses tellerini hazırlamak ve korumak", "Sesi kalınlaştırmak", "Nefesi kesmek"), 1, "Şan"),
            QuizQuestion("san7", "Alt hangi ses aralığıdır?", listOf("En ince kadın sesi", "En kalın kadın sesi", "İnce erkek sesi", "Kalın erkek sesi"), 1, "Şan"),
            QuizQuestion("san8", "Bas hangi ses aralığıdır?", listOf("İnce kadın sesi", "İnce erkek sesi", "En kalın erkek sesi", "Çocuk sesi"), 2, "Şan"),
            QuizQuestion("san9", "Legato şan tekniği nasıl yapılır?", listOf("Kesik kesik", "Notalar arası akıcı geçiş", "Staccato", "Tremolo"), 1, "Şan"),
            QuizQuestion("san10", "Şan eğitiminde 'placement' ne demektir?", listOf("Sahne pozisyonu", "Sesin rezonans noktası", "Nota yeri", "Mikrofon yeri"), 1, "Şan")
        ),
        
        "Müzik Prodüksiyon" to listOf(
            QuizQuestion("mp1", "DAW ne anlama gelir?", listOf("Digital Audio Workstation", "Direct Audio Wave", "Dynamic Audio Workshop", "Digital Analog Waveform"), 0, "Müzik Prodüksiyon"),
            QuizQuestion("mp2", "EQ ne için kullanılır?", listOf("Ses efekti", "Frekans dengeleme", "Tempo ayarlama", "Kayıt"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp3", "Compression ne yapar?", listOf("Dosya sıkıştırma", "Dinamik aralığı daraltma", "Ses yükseltme", "Tempo değiştirme"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp4", "MIDI ne demektir?", listOf("Music Information Data Interface", "Musical Instrument Digital Interface", "Media Input Digital Info", "Music In Digital Integration"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp5", "Sample rate (örnekleme hızı) standardı nedir?", listOf("22 kHz", "44.1 kHz", "96 kHz", "8 kHz"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp6", "Reverb efekti ne simüle eder?", listOf("Gecikme", "Mekan/yankı", "Distorsiyon", "Pitch değişimi"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp7", "Mixing ve Mastering arasındaki fark nedir?", listOf("Aynı şey", "Mixing:parçaları birleştir, Mastering:son dokunuş", "Mastering önce gelir", "Fark yok"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp8", "VST ne demektir?", listOf("Video Sound Track", "Virtual Studio Technology", "Voice Synthesis Tool", "Variable Sound Type"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp9", "Sidechain compression ne için kullanılır?", listOf("Ses kaydetme", "Pumping efekti/dinamik kontrol", "Pitch correction", "Time stretching"), 1, "Müzik Prodüksiyon"),
            QuizQuestion("mp10", "Bit depth ne belirler?", listOf("Dosya boyutu", "Dinamik aralık/ses kalitesi", "Tempo", "Frekans"), 1, "Müzik Prodüksiyon")
        ),
        
        // ========== EK SPOR ==========
        "Yoga" to listOf(
            QuizQuestion("yg1", "Yoga'nın kökeni neresidir?", listOf("Çin", "Hindistan", "Japonya", "Tibet"), 1, "Yoga"),
            QuizQuestion("yg2", "Asana ne demektir?", listOf("Nefes", "Poz/duruş", "Meditasyon", "Mantra"), 1, "Yoga"),
            QuizQuestion("yg3", "Pranayama ne demektir?", listOf("Poz", "Nefes kontrolü", "Meditasyon", "Esneme"), 1, "Yoga"),
            QuizQuestion("yg4", "Savasana (Corpse Pose) ne zaman yapılır?", listOf("Başlangıçta", "Dersin sonunda/dinlenme", "Isınmada", "Sadece ileri seviyede"), 1, "Yoga"),
            QuizQuestion("yg5", "Downward Dog pozu hangi bölgeleri çalıştırır?", listOf("Sadece bacak", "Tüm vücut/omuz, sırt, bacak", "Sadece kol", "Sadece karın"), 1, "Yoga"),
            QuizQuestion("yg6", "Vinyasa yoga'nın özelliği nedir?", listOf("Sabit pozlar", "Nefes ile senkronize akıcı hareket", "Sadece meditasyon", "Sıcak ortam"), 1, "Yoga"),
            QuizQuestion("yg7", "Namaste ne anlama gelir?", listOf("Hoşça kal", "İçimdeki ışık seninkini selamlar", "Merhaba", "Teşekkürler"), 1, "Yoga"),
            QuizQuestion("yg8", "Om/Aum ne sembolize eder?", listOf("Güneş", "Evrensel ses/bilinç", "Su", "Toprak"), 1, "Yoga"),
            QuizQuestion("yg9", "Hot yoga hangi sıcaklıkta yapılır?", listOf("Normal oda", "35-40°C civarı", "10°C", "50°C"), 1, "Yoga"),
            QuizQuestion("yg10", "Tree Pose (Vrksasana) ne çalıştırır?", listOf("Esneklik", "Denge ve konsantrasyon", "Güç", "Hız"), 1, "Yoga")
        ),
        
        "Boks" to listOf(
            QuizQuestion("boks1", "Boks maçı kaç raunddan oluşur (profesyonel)?", listOf("6", "10-12", "15", "20"), 1, "Boks"),
            QuizQuestion("boks2", "Jab yumruğu nasıl atılır?", listOf("Arka el ile", "Ön el ile düz", "Kanca şeklinde", "Yukarıdan aşağı"), 1, "Boks"),
            QuizQuestion("boks3", "Cross (düz yumruk) hangi elle atılır?", listOf("Ön el", "Arka el", "Her iki el", "Dirsek"), 1, "Boks"),
            QuizQuestion("boks4", "Hook yumruğu nasıl bir harekettir?", listOf("Düz", "Yandan kanca şeklinde", "Yukarıdan", "Aşağıdan"), 1, "Boks"),
            QuizQuestion("boks5", "Uppercut yumruğu nasıl atılır?", listOf("Düz", "Yandan", "Aşağıdan yukarı", "Yukarıdan aşağı"), 2, "Boks"),
            QuizQuestion("boks6", "Boks stance (duruş) nasıl olmalı?", listOf("Ayaklar bitişik", "Ayaklar omuz genişliğinde, dizler hafif kırık", "Bacaklar gergin", "Tek ayak üzerinde"), 1, "Boks"),
            QuizQuestion("boks7", "Slip hareketi ne için yapılır?", listOf("Saldırı", "Yumruktan kaçınma", "Güç artırma", "Isınma"), 1, "Boks"),
            QuizQuestion("boks8", "Bob and weave ne demektir?", listOf("Yumruk türü", "Eğilerek yumruktan kaçınma", "Ayak hareketi", "Savunma pozisyonu"), 1, "Boks"),
            QuizQuestion("boks9", "Boksta 'guard' ne demektir?", listOf("Hakem", "Koruma/savunma pozisyonu", "Köşe adamı", "Antrenör"), 1, "Boks"),
            QuizQuestion("boks10", "Southpaw ne demektir?", listOf("Sağ elini kullanan", "Solak boksör", "Ağır siklet", "Hafif siklet"), 1, "Boks")
        ),
        
        "Yüzme" to listOf(
            QuizQuestion("yzm1", "Serbest stil (freestyle) en hızlı yüzme stili midir?", listOf("Hayır", "Evet", "Kurbağalama daha hızlı", "Kelebek daha hızlı"), 1, "Yüzme"),
            QuizQuestion("yzm2", "Kurbağalama stilinin ayak hareketi nasıldır?", listOf("Yukarı aşağı", "Makas gibi", "Kurbağa bacağı gibi açılıp kapanma", "Dairesel"), 2, "Yüzme"),
            QuizQuestion("yzm3", "Kelebek stilinin kol hareketi nasıldır?", listOf("Sırayla", "İki kol aynı anda", "Tek kol", "Kol kullanılmaz"), 1, "Yüzme"),
            QuizQuestion("yzm4", "Sırtüstü yüzmede yüz hangi yöne bakar?", listOf("Aşağı", "Yukarı", "Yana", "Öne"), 1, "Yüzme"),
            QuizQuestion("yzm5", "Olimpik yüzme havuzu kaç metre uzunluğundadır?", listOf("25m", "50m", "100m", "33m"), 1, "Yüzme"),
            QuizQuestion("yzm6", "Yüzmede 'flip turn' ne demektir?", listOf("Dalış", "Duvarda takla dönüşü", "Çıkış", "Nefes tekniği"), 1, "Yüzme"),
            QuizQuestion("yzm7", "Streamline pozisyonu ne zaman kullanılır?", listOf("Sadece serbest stilde", "Dalış ve dönüşlerde", "Sadece sırtüstünde", "Hiçbir zaman"), 1, "Yüzme"),
            QuizQuestion("yzm8", "Bilateral nefes ne demektir?", listOf("Nefes tutma", "Her iki tarafa da nefes alma", "Tek tarafa nefes", "Su altında nefes"), 1, "Yüzme"),
            QuizQuestion("yzm9", "Yüzmede 'catch' fazı ne demektir?", listOf("Top yakalama", "El ile suyu kavrama fazı", "Bitiş", "Başlangıç"), 1, "Yüzme"),
            QuizQuestion("yzm10", "Hangi yüzme stili en yavaştır?", listOf("Serbest", "Kelebek", "Sırtüstü", "Kurbağalama"), 3, "Yüzme")
        ),
        
        "Tenis" to listOf(
            QuizQuestion("tn1", "Tenis skorlamasında 'love' ne demektir?", listOf("1 sayı", "0 sayı", "15 sayı", "Maç sonu"), 1, "Tenis"),
            QuizQuestion("tn2", "Tenis sayı sıralaması nasıldır?", listOf("1-2-3-4", "15-30-40-game", "10-20-30-40", "5-10-15-20"), 1, "Tenis"),
            QuizQuestion("tn3", "Deuce ne demektir?", listOf("Maç sonu", "40-40 beraberlik", "Servis hatası", "Let"), 1, "Tenis"),
            QuizQuestion("tn4", "Ace ne demektir?", listOf("Hata", "Karşılanamayan servis", "Çift hata", "Net topu"), 1, "Tenis"),
            QuizQuestion("tn5", "Grand Slam turnuvaları kaç tanedir?", listOf("2", "4", "6", "8"), 1, "Tenis"),
            QuizQuestion("tn6", "Forehand vuruşu nasıl yapılır?", listOf("Vücut önünden ters el", "Vücut önünden dominant el", "Baş üstünden", "Alttan"), 1, "Tenis"),
            QuizQuestion("tn7", "Backhand vuruşu nasıl yapılır?", listOf("Dominant el önden", "Vücut önünden ters taraf", "Servis", "Vole"), 1, "Tenis"),
            QuizQuestion("tn8", "Let ne zaman ilan edilir?", listOf("Top dışarı çıkınca", "Servis file değip doğru alana düşünce", "Oyuncu düşünce", "Yağmurda"), 1, "Tenis"),
            QuizQuestion("tn9", "Tiebreak ne zaman oynanır?", listOf("Her sette", "Set 6-6 olunca", "Her oyunda", "Sadece finalde"), 1, "Tenis"),
            QuizQuestion("tn10", "Wimbledon hangi zemin türünde oynanır?", listOf("Toprak", "Sert", "Çim", "Halı"), 2, "Tenis")
        ),
        
        "Voleybol" to listOf(
            QuizQuestion("vb1", "Voleybol takımı sahada kaç kişiden oluşur?", listOf("5", "6", "7", "8"), 1, "Voleybol"),
            QuizQuestion("vb2", "Voleybolda set kaç sayıya kadar oynanır?", listOf("15", "21", "25", "30"), 2, "Voleybol"),
            QuizQuestion("vb3", "Libero oyuncusunun özelliği nedir?", listOf("En uzun oyuncu", "Savunma uzmanı, smaç atamaz", "Sadece servis atar", "Kaptan"), 1, "Voleybol"),
            QuizQuestion("vb4", "Smaç (spike) nedir?", listOf("Servis", "Sert vuruşla topa vurma", "Blok", "Pas"), 1, "Voleybol"),
            QuizQuestion("vb5", "Voleybolda kaç kez topa dokunulabilir?", listOf("2", "3", "4", "Sınırsız"), 1, "Voleybol"),
            QuizQuestion("vb6", "Rotasyon ne zaman yapılır?", listOf("Her sayıda", "Servis kazanıldığında", "Set sonunda", "Oyuncu değişiminde"), 1, "Voleybol"),
            QuizQuestion("vb7", "Blok sayı olarak sayılır mı?", listOf("Evet, her zaman", "Blok dokunuşu sayılmaz", "Sadece başarılı blok", "Hayır"), 1, "Voleybol"),
            QuizQuestion("vb8", "Setter (pasör) ne yapar?", listOf("Smaç atar", "Topu hücumcuya hazırlar", "Sadece savunma", "Servis atar"), 1, "Voleybol"),
            QuizQuestion("vb9", "Plaj voleybolu kaç kişi ile oynanır?", listOf("4", "3", "2", "6"), 2, "Voleybol"),
            QuizQuestion("vb10", "Voleybolda file yüksekliği (erkekler) kaç cm'dir?", listOf("200 cm", "224 cm", "243 cm", "260 cm"), 2, "Voleybol")
        ),
        
        "Bisiklet" to listOf(
            QuizQuestion("bsk1", "Yol bisikletinin temel özelliği nedir?", listOf("Kalın tekerlekler", "İnce tekerlekler, hafif kadro", "Amortisör", "Geniş gidon"), 1, "Bisiklet"),
            QuizQuestion("bsk2", "MTB (dağ bisikleti) ne için tasarlanmıştır?", listOf("Yol", "Arazi ve patika", "Yarış", "Şehir içi"), 1, "Bisiklet"),
            QuizQuestion("bsk3", "Kadans ne demektir?", listOf("Hız", "Dakikadaki pedal devri", "Mesafe", "Yükseklik"), 1, "Bisiklet"),
            QuizQuestion("bsk4", "Bisiklette vites ne için kullanılır?", listOf("Fren", "Pedal direncini ayarlama", "Direksiyon", "Işık"), 1, "Bisiklet"),
            QuizQuestion("bsk5", "Tour de France ne kadar sürer?", listOf("1 hafta", "2 hafta", "3 hafta", "1 ay"), 2, "Bisiklet"),
            QuizQuestion("bsk6", "Bisiklette 'drafting' ne demektir?", listOf("Rüzgar kesme/arkadan gitme", "Hızlanma", "Fren yapma", "Vites değiştirme"), 0, "Bisiklet"),
            QuizQuestion("bsk7", "Clipless pedal ne demektir?", listOf("Pedal yok", "Ayakkabıyla kilitlenen pedal", "Düz pedal", "Plastik pedal"), 1, "Bisiklet"),
            QuizQuestion("bsk8", "Bisiklet sele yüksekliği nasıl ayarlanmalı?", listOf("En alçak", "Pedal en altta bacak hafif kırık", "En yüksek", "Fark etmez"), 1, "Bisiklet"),
            QuizQuestion("bsk9", "Aero pozisyon ne demektir?", listOf("Dik duruş", "Rüzgar direncini azaltan eğik pozisyon", "Rahat pozisyon", "Yarış pozisyonu"), 1, "Bisiklet"),
            QuizQuestion("bsk10", "BMX bisiklet ne için kullanılır?", listOf("Uzun yol", "Gösteri ve akrobasi", "Dağ", "Şehir içi"), 1, "Bisiklet")
        ),
        
        "Pilates" to listOf(
            QuizQuestion("plt1", "Pilates'i kim geliştirmiştir?", listOf("Jane Fonda", "Joseph Pilates", "Madonna", "Arnold"), 1, "Pilates"),
            QuizQuestion("plt2", "Pilates'in temel odak noktası neresidir?", listOf("Bacaklar", "Core (merkez/karın)", "Kollar", "Sırt"), 1, "Pilates"),
            QuizQuestion("plt3", "'Powerhouse' Pilates'te neyi ifade eder?", listOf("Spor salonu", "Karın, sırt, kalça kasları", "Ekipman", "Egzersiz türü"), 1, "Pilates"),
            QuizQuestion("plt4", "Reformer ne demektir?", listOf("Kıyafet", "Yaylı Pilates ekipmanı", "Egzersiz türü", "Nefes tekniği"), 1, "Pilates"),
            QuizQuestion("plt5", "Mat Pilates neyin üzerinde yapılır?", listOf("Makinede", "Yerde/mat üzerinde", "Havuzda", "Topun üzerinde"), 1, "Pilates"),
            QuizQuestion("plt6", "Pilates'te nefes nasıl alınır?", listOf("Ağızdan", "Burundan al, ağızdan ver", "Nefes tutarak", "Hızlı"), 1, "Pilates"),
            QuizQuestion("plt7", "'Hundred' egzersizi ne çalıştırır?", listOf("Bacak", "Core ve nefes", "Kol", "Omuz"), 1, "Pilates"),
            QuizQuestion("plt8", "Pilates'te kaç temel prensip vardır?", listOf("3", "6", "8", "10"), 1, "Pilates"),
            QuizQuestion("plt9", "Pilates yoga'dan farkı nedir?", listOf("Aynı", "Fiziksel güç odaklı, makineli seçenekler", "Sadece meditasyon", "Fark yok"), 1, "Pilates"),
            QuizQuestion("plt10", "Roll-up hareketi ne çalıştırır?", listOf("Bacak", "Karın ve omurga esnekliği", "Kol", "Kalça"), 1, "Pilates")
        ),
        
        // ========== EK DİLLER ==========
        "Fransızca" to listOf(
            QuizQuestion("fr1", "'Bonjour' ne demektir?", listOf("İyi geceler", "Günaydın/Merhaba", "Hoşça kal", "Teşekkürler"), 1, "Fransızca"),
            QuizQuestion("fr2", "Fransızca'da 'je suis' ne demektir?", listOf("Sen varsın", "Ben varım/im", "O var", "Biz varız"), 1, "Fransızca"),
            QuizQuestion("fr3", "Fransızca'da kaç tane artikel vardır?", listOf("1", "2", "3", "4"), 2, "Fransızca"),
            QuizQuestion("fr4", "'Merci' ne demektir?", listOf("Merhaba", "Teşekkürler", "Evet", "Hayır"), 1, "Fransızca"),
            QuizQuestion("fr5", "Fransızca'da 'manger' fiili ne anlama gelir?", listOf("İçmek", "Yemek", "Uyumak", "Gitmek"), 1, "Fransızca"),
            QuizQuestion("fr6", "'Comment ça va?' ne demektir?", listOf("Adın ne?", "Nasılsın?", "Nerelisin?", "Kaç yaşındasın?"), 1, "Fransızca"),
            QuizQuestion("fr7", "Fransızca'da geçmiş zaman (passé composé) nasıl oluşturulur?", listOf("Sadece fiil", "avoir/être + participe passé", "Sadece ek", "Değişmez"), 1, "Fransızca"),
            QuizQuestion("fr8", "'Au revoir' ne demektir?", listOf("Merhaba", "Hoşça kal", "Teşekkürler", "Özür dilerim"), 1, "Fransızca"),
            QuizQuestion("fr9", "Fransızca'da 'le' ve 'la' farkı nedir?", listOf("Tekil/çoğul", "Eril/dişil artikel", "Büyük/küçük", "Soru/cevap"), 1, "Fransızca"),
            QuizQuestion("fr10", "'Je t'aime' ne demektir?", listOf("Görüşürüz", "Seni seviyorum", "Teşekkürler", "Merhaba"), 1, "Fransızca")
        ),
        
        "İtalyanca" to listOf(
            QuizQuestion("it1", "'Ciao' ne demektir?", listOf("Hoşça kal/Merhaba", "Teşekkürler", "Evet", "Hayır"), 0, "İtalyanca"),
            QuizQuestion("it2", "'Buongiorno' ne demektir?", listOf("İyi akşamlar", "Günaydın/İyi günler", "İyi geceler", "Hoşça kal"), 1, "İtalyanca"),
            QuizQuestion("it3", "İtalyanca'da artikel örneği hangisidir?", listOf("le", "der", "il/lo/la", "die"), 2, "İtalyanca"),
            QuizQuestion("it4", "'Grazie' ne demektir?", listOf("Lütfen", "Teşekkürler", "Merhaba", "Nasılsın"), 1, "İtalyanca"),
            QuizQuestion("it5", "Fiiller hangi çekim gruplarına ayrılır?", listOf("-are/-ere/-ire", "-ir/-re/-ar", "-en/-er/-ir", "Yok"), 0, "İtalyanca"),
            QuizQuestion("it6", "'Come stai?' ne demektir?", listOf("Neredesin?", "Nasılsın?", "Adın ne?", "Ne zaman?"), 1, "İtalyanca"),
            QuizQuestion("it7", "'Io sono' ne demektir?", listOf("Sen varsın", "Ben varım", "O var", "Biz varız"), 1, "İtalyanca"),
            QuizQuestion("it8", "Geçmiş zaman passato prossimo nasıl yapılır?", listOf("Sadece fiil", "avere/essere + participio passato", "Sadece ek", "Yok"), 1, "İtalyanca"),
            QuizQuestion("it9", "'Arrivederci' ne demektir?", listOf("Merhaba", "Güle güle/Hoşça kal", "Teşekkürler", "Lütfen"), 1, "İtalyanca"),
            QuizQuestion("it10", "İtalyanca'da 'per favore' ne demektir?", listOf("Özür dilerim", "Lütfen", "Teşekkürler", "İyi geceler"), 1, "İtalyanca")
        ),

        "İspanyolca" to listOf(
            QuizQuestion("es1", "'Hola' ne demektir?", listOf("Hoşça kal", "Merhaba", "Teşekkürler", "Evet"), 1, "İspanyolca"),
            QuizQuestion("es2", "İspanyolca'da 'yo soy' ne demektir?", listOf("Sen varsın", "Ben varım/im", "O var", "Biz varız"), 1, "İspanyolca"),
            QuizQuestion("es3", "'Gracias' ne demektir?", listOf("Merhaba", "Hoşça kal", "Teşekkürler", "Lütfen"), 2, "İspanyolca"),
            QuizQuestion("es4", "İspanyolca'da ters soru işareti (¿) ne zaman kullanılır?", listOf("Soru sonunda", "Soru başında", "Ünlem için", "Kullanılmaz"), 1, "İspanyolca"),
            QuizQuestion("es5", "'¿Cómo estás?' ne demektir?", listOf("Adın ne?", "Nasılsın?", "Nerelisin?", "Ne yapıyorsun?"), 1, "İspanyolca"),
            QuizQuestion("es6", "İspanyolca'da 'ser' ve 'estar' farkı nedir?", listOf("Aynı", "Kalıcı/geçici olmak", "Tekil/çoğul", "Geçmiş/gelecek"), 1, "İspanyolca"),
            QuizQuestion("es7", "'Buenos días' ne demektir?", listOf("İyi geceler", "Günaydın", "İyi akşamlar", "Hoşça kal"), 1, "İspanyolca"),
            QuizQuestion("es8", "İspanyolca'da 'hablar' fiili ne anlama gelir?", listOf("Yürümek", "Konuşmak", "Yemek", "Uyumak"), 1, "İspanyolca"),
            QuizQuestion("es9", "'Adiós' ne demektir?", listOf("Merhaba", "Teşekkürler", "Hoşça kal", "Evet"), 2, "İspanyolca"),
            QuizQuestion("es10", "İspanyolca hangi kıtada en çok konuşulur?", listOf("Avrupa", "Güney Amerika", "Afrika", "Asya"), 1, "İspanyolca")
        ),

        "Arapça" to listOf(
            QuizQuestion("ar1", "'Merhaba' Arapça'da nasıl söylenir?", listOf("Hola", "Hello", "Marhaban/مرحبا", "Bonjour"), 2, "Arapça"),
            QuizQuestion("ar2", "Arapça hangi yazı yönünü kullanır?", listOf("Soldan sağa", "Sağdan sola", "Yukarıdan aşağı", "Dairesel"), 1, "Arapça"),
            QuizQuestion("ar3", "Arap alfabesi kaç harflidir?", listOf("22", "24", "28", "32"), 2, "Arapça"),
            QuizQuestion("ar4", "'Shukran/شكراً' ne demektir?", listOf("Lütfen", "Teşekkürler", "Merhaba", "Hoşça kal"), 1, "Arapça"),
            QuizQuestion("ar5", "'Ana' zamiri ne anlama gelir?", listOf("Sen", "Ben", "O", "Biz"), 1, "Arapça"),
            QuizQuestion("ar6", "'Keif halak?/كيف حالك؟' ne demektir?", listOf("Adın ne?", "Nasılsın?", "Neredesin?", "Ne zaman?"), 1, "Arapça"),
            QuizQuestion("ar7", "Arapça'da dişil/eril farkı var mıdır?", listOf("Yok", "Evet, kelimeler eril/dişil olabilir", "Sadece zamirlerde", "Sadece sayılarda"), 1, "Arapça"),
            QuizQuestion("ar8", "Tashkeel (hareke) ne işe yarar?", listOf("Renk", "Sesli harf/vurgu belirtir", "Sayı", "Zaman"), 1, "Arapça"),
            QuizQuestion("ar9", "'Ma'a as-salama/مع السلامة' ne demektir?", listOf("Teşekkürler", "Güle güle/Hoşça kal", "Merhaba", "Lütfen"), 1, "Arapça"),
            QuizQuestion("ar10", "Kuran-ı Kerim hangi dilde yazılmıştır?", listOf("Türkçe", "Arapça", "Farsça", "İbranice"), 1, "Arapça")
        ),
        
        "Japonca" to listOf(
            QuizQuestion("jp1", "Japonca'da kaç yazı sistemi vardır?", listOf("1", "2", "3", "4"), 2, "Japonca"),
            QuizQuestion("jp2", "Hiragana ne için kullanılır?", listOf("Yabancı kelimeler", "Japon kelimeleri", "Kanji", "Sayılar"), 1, "Japonca"),
            QuizQuestion("jp3", "Katakana ne için kullanılır?", listOf("Japon kelimeleri", "Yabancı kelimeler", "Gramer", "Fiiller"), 1, "Japonca"),
            QuizQuestion("jp4", "'Konnichiwa' ne demektir?", listOf("Günaydın", "Merhaba (gündüz)", "İyi geceler", "Hoşça kal"), 1, "Japonca"),
            QuizQuestion("jp5", "'Arigatou' ne demektir?", listOf("Merhaba", "Hoşça kal", "Teşekkürler", "Özür dilerim"), 2, "Japonca"),
            QuizQuestion("jp6", "Japonca'da 'desu' ne işlev görür?", listOf("Soru eki", "Kibarlık/olmak fiili", "Olumsuzluk", "Geçmiş zaman"), 1, "Japonca"),
            QuizQuestion("jp7", "'Sayonara' ne demektir?", listOf("Merhaba", "Teşekkürler", "Hoşça kal", "Evet"), 2, "Japonca"),
            QuizQuestion("jp8", "Japonca'da cümle yapısı nasıldır?", listOf("Özne-Fiil-Nesne", "Özne-Nesne-Fiil", "Fiil-Özne-Nesne", "Nesne-Özne-Fiil"), 1, "Japonca"),
            QuizQuestion("jp9", "'Hai' ve 'Iie' ne demektir?", listOf("Merhaba/Hoşçakal", "Evet/Hayır", "Lütfen/Teşekkürler", "Ben/Sen"), 1, "Japonca"),
            QuizQuestion("jp10", "Kanji nereden gelir?", listOf("Kore", "Çin", "Hindistan", "Japonya'da gelişti"), 1, "Japonca")
        ),
        
        "Korece" to listOf(
            QuizQuestion("kr1", "Kore alfabesinin adı nedir?", listOf("Kanji", "Hangul", "Hiragana", "Pinyin"), 1, "Korece"),
            QuizQuestion("kr2", "Hangul'u kim icat etmiştir?", listOf("Halk", "Kral Sejong", "Çin imparatoru", "Budist rahipler"), 1, "Korece"),
            QuizQuestion("kr3", "'Annyeonghaseyo' ne demektir?", listOf("Teşekkürler", "Merhaba", "Hoşça kal", "Özür dilerim"), 1, "Korece"),
            QuizQuestion("kr4", "'Kamsahamnida' ne demektir?", listOf("Merhaba", "Hoşça kal", "Teşekkürler", "Özür dilerim"), 2, "Korece"),
            QuizQuestion("kr5", "Korece'de saygı dili (존댓말) ne zaman kullanılır?", listOf("Arkadaşlarla", "Büyüklerle/resmi durumlarda", "Çocuklarla", "Hiçbir zaman"), 1, "Korece"),
            QuizQuestion("kr6", "'Ne' (네) ve 'Aniyo' (아니요) ne demektir?", listOf("Merhaba/Hoşçakal", "Evet/Hayır", "Lütfen/Teşekkürler", "Ben/Sen"), 1, "Korece"),
            QuizQuestion("kr7", "Korece cümle yapısı nasıldır?", listOf("Özne-Fiil-Nesne", "Özne-Nesne-Fiil", "Fiil-Özne-Nesne", "Nesne-Özne-Fiil"), 1, "Korece"),
            QuizQuestion("kr8", "'Annyeonghi gaseyo' ne zaman söylenir?", listOf("Gelene", "Gidene (siz kalırken)", "Sabah", "Akşam"), 1, "Korece"),
            QuizQuestion("kr9", "Hangul'da kaç temel sesli harf vardır?", listOf("5", "10", "14", "21"), 1, "Korece"),
            QuizQuestion("kr10", "'Oppa' ve 'Unnie' ne demektir?", listOf("Anne/Baba", "Abi/Abla", "Arkadaş/Düşman", "Hoca/Öğrenci"), 1, "Korece")
        ),
        
        // ========== EK AKADEMİK ==========
        "Kimya" to listOf(
            QuizQuestion("kim1", "Suyun kimyasal formülü nedir?", listOf("CO2", "H2O", "NaCl", "O2"), 1, "Kimya"),
            QuizQuestion("kim2", "Periyodik tablodaki ilk element nedir?", listOf("Helyum", "Hidrojen", "Karbon", "Oksijen"), 1, "Kimya"),
            QuizQuestion("kim3", "pH 7 ne anlama gelir?", listOf("Asidik", "Nötr", "Bazik", "Belirsiz"), 1, "Kimya"),
            QuizQuestion("kim4", "NaCl'nin yaygın adı nedir?", listOf("Şeker", "Sofra tuzu", "Sirke", "Su"), 1, "Kimya"),
            QuizQuestion("kim5", "Avogadro sayısı yaklaşık kaçtır?", listOf("3.14", "6.02 × 10²³", "9.8", "299,792,458"), 1, "Kimya"),
            QuizQuestion("kim6", "Asit ve bazın reaksiyonuna ne denir?", listOf("Yanma", "Nötrleşme", "Oksitlenme", "İndirgeme"), 1, "Kimya"),
            QuizQuestion("kim7", "Elektron, proton ve nötron nerede bulunur?", listOf("Hepsi çekirdekte", "Elektron:yörünge, proton/nötron:çekirdek", "Hepsi yörüngede", "Rastgele"), 1, "Kimya"),
            QuizQuestion("kim8", "CO2'nin adı nedir?", listOf("Karbon monoksit", "Karbondioksit", "Metan", "Ozon"), 1, "Kimya"),
            QuizQuestion("kim9", "Organik kimya neyi inceler?", listOf("Metaller", "Karbon bileşikleri", "Gazlar", "Tuzlar"), 1, "Kimya"),
            QuizQuestion("kim10", "Mol kavramı neyi ifade eder?", listOf("Kütle birimi", "Madde miktarı birimi", "Hacim birimi", "Sıcaklık birimi"), 1, "Kimya")
        ),
        
        "Biyoloji" to listOf(
            QuizQuestion("bio1", "DNA'nın açılımı nedir?", listOf("Deoksi Nükleik Asit", "Deoksiribonükleik Asit", "Di-Nitro Asit", "Dinamik Nükleer Asit"), 1, "Biyoloji"),
            QuizQuestion("bio2", "Hücrenin enerji santrali hangisidir?", listOf("Çekirdek", "Mitokondri", "Ribozom", "Golgi"), 1, "Biyoloji"),
            QuizQuestion("bio3", "Fotosentez nerede gerçekleşir?", listOf("Mitokondri", "Kloroplast", "Çekirdek", "Ribozom"), 1, "Biyoloji"),
            QuizQuestion("bio4", "İnsan vücudunda kaç kromozom vardır?", listOf("23", "46", "48", "44"), 1, "Biyoloji"),
            QuizQuestion("bio5", "Homeostatik ne demektir?", listOf("Hastalık", "İç dengenin korunması", "Üreme", "Sindirim"), 1, "Biyoloji"),
            QuizQuestion("bio6", "Enzimler ne iş yapar?", listOf("Enerji depolar", "Kimyasal reaksiyonları hızlandırır", "Genetik bilgi taşır", "Kas oluşturur"), 1, "Biyoloji"),
            QuizQuestion("bio7", "Ekosistemdeki üreticiler kimlerdir?", listOf("Hayvanlar", "Mantarlar", "Bitkiler", "Bakteriler"), 2, "Biyoloji"),
            QuizQuestion("bio8", "Mutasyon ne demektir?", listOf("Hücre bölünmesi", "DNA'daki değişiklik", "Protein sentezi", "Solunum"), 1, "Biyoloji"),
            QuizQuestion("bio9", "Osmoz nedir?", listOf("Gaz değişimi", "Suyun yarı geçirgen zardan geçişi", "Besin sindirimi", "Kan dolaşımı"), 1, "Biyoloji"),
            QuizQuestion("bio10", "Prokaryot ve ökaryot farkı nedir?", listOf("Boyut", "Çekirdek zarı var/yok", "Renk", "Hareket"), 1, "Biyoloji")
        ),
        
        "Tarih" to listOf(
            QuizQuestion("tar1", "İstanbul'un fethi hangi yılda gerçekleşti?", listOf("1071", "1453", "1299", "1923"), 1, "Tarih"),
            QuizQuestion("tar2", "Fransız Devrimi hangi yılda başladı?", listOf("1776", "1789", "1848", "1917"), 1, "Tarih"),
            QuizQuestion("tar3", "I. Dünya Savaşı hangi yıllar arasında oldu?", listOf("1900-1904", "1914-1918", "1939-1945", "1950-1953"), 1, "Tarih"),
            QuizQuestion("tar4", "Türkiye Cumhuriyeti ne zaman ilan edildi?", listOf("1919", "1920", "1922", "1923"), 3, "Tarih"),
            QuizQuestion("tar5", "Sanayi Devrimi hangi ülkede başladı?", listOf("Fransa", "Almanya", "İngiltere", "ABD"), 2, "Tarih"),
            QuizQuestion("tar6", "Rönesans hangi ülkede başladı?", listOf("Fransa", "İtalya", "İngiltere", "Almanya"), 1, "Tarih"),
            QuizQuestion("tar7", "Magna Carta hangi yılda imzalandı?", listOf("1066", "1215", "1453", "1776"), 1, "Tarih"),
            QuizQuestion("tar8", "Amerika'nın keşfi hangi yılda gerçekleşti?", listOf("1453", "1492", "1776", "1620"), 1, "Tarih"),
            QuizQuestion("tar9", "Soğuk Savaş hangi iki güç arasındaydı?", listOf("ABD-Çin", "ABD-SSCB", "İngiltere-Fransa", "Almanya-Rusya"), 1, "Tarih"),
            QuizQuestion("tar10", "II. Dünya Savaşı'nda atom bombası hangi şehirlere atıldı?", listOf("Tokyo-Osaka", "Hiroşima-Nagazaki", "Berlin-Hamburg", "Londra-Paris"), 1, "Tarih")
        ),
        
        "Psikoloji" to listOf(
            QuizQuestion("psi1", "Psikolojinin babası olarak kabul edilen kimdir?", listOf("Freud", "Wilhelm Wundt", "Skinner", "Pavlov"), 1, "Psikoloji"),
            QuizQuestion("psi2", "Id, ego, süperego kavramları kime aittir?", listOf("Jung", "Freud", "Skinner", "Piaget"), 1, "Psikoloji"),
            QuizQuestion("psi3", "Klasik koşullanmayı kim keşfetti?", listOf("Skinner", "Freud", "Pavlov", "Watson"), 2, "Psikoloji"),
            QuizQuestion("psi4", "Maslow'un ihtiyaçlar hiyerarşisinin en üstünde ne var?", listOf("Güvenlik", "Fizyolojik", "Kendini gerçekleştirme", "Sevgi"), 2, "Psikoloji"),
            QuizQuestion("psi5", "Bilişsel psikoloji neyi inceler?", listOf("Beyin yapısı", "Düşünce ve zihinsel süreçler", "Sosyal davranış", "Kişilik"), 1, "Psikoloji"),
            QuizQuestion("psi6", "DSM ne anlama gelir?", listOf("Tanı ve İstatistik El Kitabı", "Dinamik Sistem Modeli", "Derin Sinir Mekanizması", "Davranış Standart Modeli"), 0, "Psikoloji"),
            QuizQuestion("psi7", "Operant koşullanma kime aittir?", listOf("Pavlov", "Freud", "B.F. Skinner", "Bandura"), 2, "Psikoloji"),
            QuizQuestion("psi8", "Sosyal öğrenme teorisi kime aittir?", listOf("Skinner", "Bandura", "Freud", "Pavlov"), 1, "Psikoloji"),
            QuizQuestion("psi9", "Placebo etkisi ne demektir?", listOf("Gerçek tedavi", "Sahte tedavinin iyileştirici etkisi", "Yan etki", "Bağımlılık"), 1, "Psikoloji"),
            QuizQuestion("psi10", "REM uykusu ile ne ilişkilidir?", listOf("Derin uyku", "Rüya görme", "Horlama", "Uyku apnesi"), 1, "Psikoloji")
        ),
        
        "Ekonomi" to listOf(
            QuizQuestion("eko1", "Arz ve talep eğrileri nerede kesişir?", listOf("Maksimum noktada", "Denge noktasında", "Minimum noktada", "Kesişmez"), 1, "Ekonomi"),
            QuizQuestion("eko2", "GDP ne anlama gelir?", listOf("Genel Dağılım Planı", "Gayri Safi Yurtiçi Hasıla", "Global Değer Payı", "Güçlü Döviz Paritesi"), 1, "Ekonomi"),
            QuizQuestion("eko3", "Enflasyon ne demektir?", listOf("Fiyatların düşmesi", "Fiyatların genel artışı", "İşsizlik", "Durgunluk"), 1, "Ekonomi"),
            QuizQuestion("eko4", "Merkez bankasının temel görevi nedir?", listOf("Kredi vermek", "Para politikası yönetimi", "Vergi toplamak", "Bütçe yapmak"), 1, "Ekonomi"),
            QuizQuestion("eko5", "Fırsat maliyeti ne demektir?", listOf("En düşük maliyet", "Vazgeçilen alternatifin değeri", "Toplam maliyet", "Kar"), 1, "Ekonomi"),
            QuizQuestion("eko6", "Monopol piyasa ne demektir?", listOf("Çok satıcı", "Tek satıcı", "Çok alıcı", "Devlet kontrolü"), 1, "Ekonomi"),
            QuizQuestion("eko7", "Adam Smith'in ünlü eseri hangisidir?", listOf("Kapital", "Ulusların Zenginliği", "Genel Teori", "Prens"), 1, "Ekonomi"),
            QuizQuestion("eko8", "Deflasyon ne demektir?", listOf("Fiyat artışı", "Fiyatların genel düşüşü", "Para basımı", "Faiz artışı"), 1, "Ekonomi"),
            QuizQuestion("eko9", "Mikroekonomi ne inceler?", listOf("Ülke ekonomisi", "Bireysel karar birimleri", "Dünya ekonomisi", "Para politikası"), 1, "Ekonomi"),
            QuizQuestion("eko10", "Fiskal politika ne demektir?", listOf("Para politikası", "Maliye/bütçe politikası", "Dış ticaret", "Sanayi politikası"), 1, "Ekonomi")
        ),
        
        // ========== SANAT ==========
        "Resim" to listOf(
            QuizQuestion("res1", "Primer renkler hangileridir?", listOf("Yeşil, turuncu, mor", "Kırmızı, mavi, sarı", "Siyah, beyaz, gri", "Pembe, turkuaz, kahve"), 1, "Resim"),
            QuizQuestion("res2", "Sekonder renkler nasıl elde edilir?", listOf("Tek renk", "İki primer rengin karışımı", "Siyah eklenerek", "Beyaz eklenerek"), 1, "Resim"),
            QuizQuestion("res3", "Perspektif ne demektir?", listOf("Renk teorisi", "Derinlik ve uzaklık yanılsaması", "Fırça tekniği", "Kağıt türü"), 1, "Resim"),
            QuizQuestion("res4", "Empresyonizm akımının öncülerinden biri kimdir?", listOf("Picasso", "Monet", "Da Vinci", "Van Gogh"), 1, "Resim"),
            QuizQuestion("res5", "Mona Lisa'yı kim yapmıştır?", listOf("Michelangelo", "Leonardo da Vinci", "Raphael", "Botticelli"), 1, "Resim"),
            QuizQuestion("res6", "Sulu boya tekniğinin özelliği nedir?", listOf("Kalın tabaka", "Şeffaf katmanlar", "Hızlı kuruma yok", "Sadece siyah beyaz"), 1, "Resim"),
            QuizQuestion("res7", "Yağlı boya avantajı nedir?", listOf("Hızlı kurur", "Yavaş kurur, düzeltme yapılabilir", "Ucuz", "Sadece kağıtta kullanılır"), 1, "Resim"),
            QuizQuestion("res8", "Kontrast ne demektir?", listOf("Benzer renkler", "Zıt değerler arasındaki fark", "Tek renk", "Karışık renkler"), 1, "Resim"),
            QuizQuestion("res9", "Kompozisyon ne demektir?", listOf("Renk karışımı", "Elemanların düzenlenmesi", "Fırça türü", "Tuval boyutu"), 1, "Resim"),
            QuizQuestion("res10", "Kübizm akımının öncüsü kimdir?", listOf("Monet", "Van Gogh", "Picasso", "Rembrandt"), 2, "Resim")
        ),
        
        "Fotoğrafçılık" to listOf(
            QuizQuestion("foto1", "Pozlama üçgeni hangi üç öğeden oluşur?", listOf("ISO, diyafram, enstantane", "ISO, beyaz ayarı, zoom", "Diyafram, zoom, flaş", "ISO, flaş, fokus"), 0, "Fotoğrafçılık"),
            QuizQuestion("foto2", "Diyafram değeri (f/stop) küçüldükçe ne olur?", listOf("Daha az ışık", "Daha çok ışık ve daha sığ alan derinliği", "Fark yok", "Sadece flaş artar"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto3", "Enstantane hızı düşerse ne olur?", listOf("Hızlı dondurma", "Daha çok hareket bulanıklığı", "ISO artar", "Renk doygunluğu artar"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto4", "ISO artırmanın yan etkisi nedir?", listOf("Keskinlik artar", "Noise/gren artar", "Renk kaybolur", "Flaş kapanır"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto5", "RAW formatı neden tercih edilir?", listOf("Daha küçük dosya", "Daha fazla düzenleme esnekliği", "Daha hızlı çekim", "Daha az dinamik aralık"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto6", "Bokeh etkisi nasıl elde edilir?", listOf("Küçük sensör", "Geniş diyafram (düşük f değerleri)", "Yüksek ISO", "Yavaş enstantane"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto7", "Beyaz ayarı (white balance) ne işe yarar?", listOf("Zoom", "Renk sıcaklığını düzeltir", "Netlik", "Pozlama"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto8", "Üçte bir kuralı ne için kullanılır?", listOf("Işık ölçümü", "Kompozisyon yerleşimi", "Renk düzeni", "Fokus"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto9", "Tripod ne sağlar?", listOf("Renk düzeltir", "Stabilizasyon, uzun pozlama desteği", "Zoom yapar", "ISO düşürür"), 1, "Fotoğrafçılık"),
            QuizQuestion("foto10", "HDR çekim ne zaman kullanılır?", listOf("Hızlı hareket", "Yüksek kontrastlı sahnelerde detay için", "Spor çekimi", "Portrelerde her zaman"), 1, "Fotoğrafçılık")
        ),

        "Video Edit" to listOf(
            QuizQuestion("vid1", "Frame rate (kare hızı) ne demektir?", listOf("Dosya boyutu", "Saniyedeki kare sayısı", "Çözünürlük", "Ses kalitesi"), 1, "Video Edit"),
            QuizQuestion("vid2", "1080p çözünürlük ne demektir?", listOf("1080x720", "1920x1080", "3840x2160", "1280x720"), 1, "Video Edit"),
            QuizQuestion("vid3", "4K çözünürlük kaç pikseldir?", listOf("1920x1080", "2560x1440", "3840x2160", "1280x720"), 2, "Video Edit"),
            QuizQuestion("vid4", "B-roll footage ne demektir?", listOf("Ana çekim", "Destekleyici/ek görüntüler", "Son sahne", "İlk sahne"), 1, "Video Edit"),
            QuizQuestion("vid5", "Color grading ne demektir?", listOf("Ses düzenleme", "Renk düzeltme ve atmosfer oluşturma", "Kesim", "Efekt ekleme"), 1, "Video Edit"),
            QuizQuestion("vid6", "J-cut ve L-cut ne demektir?", listOf("Geçiş efekti", "Ses ve görüntü zamanlaması teknikleri", "Kamera hareketi", "Zoom türleri"), 1, "Video Edit"),
            QuizQuestion("vid7", "Render ne demektir?", listOf("Silme", "Video dosyasını oluşturma/dışa aktarma", "İçe aktarma", "Kesme"), 1, "Video Edit"),
            QuizQuestion("vid8", "Keyframe ne için kullanılır?", listOf("Silme", "Animasyon ve efekt değişim noktaları", "Ses ekleme", "Metin ekleme"), 1, "Video Edit"),
            QuizQuestion("vid9", "Codec ne demektir?", listOf("Video türü", "Sıkıştırma/açma algoritması", "Dosya uzantısı", "Yazılım"), 1, "Video Edit"),
            QuizQuestion("vid10", "Aspect ratio (en-boy oranı) 16:9 ne anlama gelir?", listOf("Kare", "Geniş ekran formatı", "Dikey video", "Sinema formatı"), 1, "Video Edit")
        ),
        
        // ========== KALAN SPORLAR ==========
        "Dövüş Sanatları" to listOf(
            QuizQuestion("ds1", "Karate hangi ülkeden gelir?", listOf("Çin", "Japonya", "Kore", "Tayland"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds2", "Taekwondo'nun odak noktası nedir?", listOf("Yumruk", "Ayak teknikleri", "Güreş", "Silah"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds3", "Judo'nun temel prensibi nedir?", listOf("Sertlik", "Yumuşaklık/Rakibin gücünü kullanma", "Hız", "Dayanıklılık"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds4", "Kung Fu hangi ülkeden gelir?", listOf("Japonya", "Çin", "Kore", "Vietnam"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds5", "BJJ (Brazilian Jiu-Jitsu) neye odaklanır?", listOf("Vuruş", "Yer teknikleri ve kilit", "Silah", "Ayak teknikleri"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds6", "MMA ne anlama gelir?", listOf("Modern Martial Arts", "Mixed Martial Arts", "Master Martial Academy", "Muscle Martial Athletics"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds7", "Muay Thai hangi ülkenin sporu?", listOf("Japonya", "Çin", "Tayland", "Filipinler"), 2, "Dövüş Sanatları"),
            QuizQuestion("ds8", "Krav Maga hangi ülkede geliştirildi?", listOf("Rusya", "İsrail", "ABD", "Brezilya"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds9", "Aikido'nun felsefesi nedir?", listOf("Saldırı", "Savunma ve uyum", "Rekabet", "Güç"), 1, "Dövüş Sanatları"),
            QuizQuestion("ds10", "Kata ne demektir?", listOf("Maç", "Form/hareket dizisi", "Kuşak", "Salon"), 1, "Dövüş Sanatları")
        ),
        
        "Koşu" to listOf(
            QuizQuestion("kos1", "Maraton mesafesi kaç kilometredir?", listOf("21.1 km", "42.195 km", "50 km", "30 km"), 1, "Koşu"),
            QuizQuestion("kos2", "Yarı maraton kaç kilometredir?", listOf("10 km", "15 km", "21.1 km", "25 km"), 2, "Koşu"),
            QuizQuestion("kos3", "Koşuda kadans ne demektir?", listOf("Hız", "Dakikadaki adım sayısı", "Mesafe", "Kalp atışı"), 1, "Koşu"),
            QuizQuestion("kos4", "Tempo koşusu ne demektir?", listOf("Yavaş koşu", "Eşik hızında sürekli koşu", "Sprint", "Dinlenme"), 1, "Koşu"),
            QuizQuestion("kos5", "Fartlek antrenmanı ne demektir?", listOf("Sabit tempo", "Hız değişimli koşu", "Uzun mesafe", "Kısa mesafe"), 1, "Koşu"),
            QuizQuestion("kos6", "VO2 max ne ölçer?", listOf("Kas gücü", "Maksimum oksijen kullanımı", "Esneklik", "Hız"), 1, "Koşu"),
            QuizQuestion("kos7", "Pronasyon ne demektir?", listOf("Hız artışı", "Ayağın içe dönmesi", "Ayağın dışa dönmesi", "Duruş"), 1, "Koşu"),
            QuizQuestion("kos8", "Negative split ne demektir?", listOf("Düşüş", "İkinci yarıyı daha hızlı koşma", "Yavaşlama", "Duraklatma"), 1, "Koşu"),
            QuizQuestion("kos9", "Easy run (kolay koşu) ne için yapılır?", listOf("Hız geliştirme", "Toparlanma ve aerobik baz", "Kas yapma", "Sprint"), 1, "Koşu"),
            QuizQuestion("kos10", "Interval antrenmanı ne demektir?", listOf("Sürekli koşu", "Hızlı-yavaş periyodlar", "Yürüyüş", "Tek tempo"), 1, "Koşu")
        ),
        
        // ========== KALAN AKADEMİK ==========
        "Edebiyat" to listOf(
            QuizQuestion("edb1", "Roman hangi edebi türe girer?", listOf("Şiir", "Nesir/düzyazı", "Tiyatro", "Deneme"), 1, "Edebiyat"),
            QuizQuestion("edb2", "Metafor ne demektir?", listOf("Benzetme", "Karşılaştırma olmadan benzetme", "Abartı", "Kişileştirme"), 1, "Edebiyat"),
            QuizQuestion("edb3", "Shakespeare'in ünlü trajedisi hangisidir?", listOf("Komedi", "Hamlet", "Pinokyo", "Don Kişot"), 1, "Edebiyat"),
            QuizQuestion("edb4", "Osmanlı Divan edebiyatının özelliği nedir?", listOf("Sade dil", "Ağır, süslü dil, Arapça-Farsça etkisi", "Halk dili", "Batı etkisi"), 1, "Edebiyat"),
            QuizQuestion("edb5", "Realizm akımı neyi savunur?", listOf("Hayali dünyalar", "Gerçekliğin olduğu gibi yansıtılması", "Romantizm", "Sembolizm"), 1, "Edebiyat"),
            QuizQuestion("edb6", "İroni ne demektir?", listOf("Övgü", "Söylenenin tersini kastetme", "Abartı", "Tekrar"), 1, "Edebiyat"),
            QuizQuestion("edb7", "Nazım Hikmet hangi şiir türüyle tanınır?", listOf("Divan şiiri", "Serbest şiir", "Aruz şiiri", "Hece şiiri"), 1, "Edebiyat"),
            QuizQuestion("edb8", "Protagonist ne demektir?", listOf("Yazar", "Ana karakter", "Anlatıcı", "Yardımcı karakter"), 1, "Edebiyat"),
            QuizQuestion("edb9", "Antagonist ne demektir?", listOf("Kahraman", "Karşıt karakter", "Anlatıcı", "Yazar"), 1, "Edebiyat"),
            QuizQuestion("edb10", "Dystopia ne demektir?", listOf("Ütopya", "Karanlık/olumsuz gelecek tasviri", "Masal", "Tarih"), 1, "Edebiyat")
        ),
        
        "Mühendislik" to listOf(
            QuizQuestion("muh1", "Newton'un hareket yasaları kaç tanedir?", listOf("1", "2", "3", "4"), 2, "Mühendislik"),
            QuizQuestion("muh2", "Gerilme (stress) ne demektir?", listOf("Uzama", "Kuvvet/alan", "Hız", "İvme"), 1, "Mühendislik"),
            QuizQuestion("muh3", "CAD ne anlama gelir?", listOf("Computer Aided Design", "Central Admin Device", "Code And Debug", "Circuit Analysis Data"), 0, "Mühendislik"),
            QuizQuestion("muh4", "Moment (tork) ne demektir?", listOf("Zaman", "Döndürme kuvveti", "Hız", "İvme"), 1, "Mühendislik"),
            QuizQuestion("muh5", "Termodinamiğin 1. yasası neyi ifade eder?", listOf("Entropi", "Enerjinin korunumu", "Isı akışı", "Basınç"), 1, "Mühendislik"),
            QuizQuestion("muh6", "Elastisite modülü (Young modülü) ne ölçer?", listOf("Sıcaklık", "Malzemenin sertliği", "Hız", "Basınç"), 1, "Mühendislik"),
            QuizQuestion("muh7", "FEM (Sonlu Elemanlar Metodu) ne için kullanılır?", listOf("Programlama", "Yapısal analiz", "Ekonomi", "Kimya"), 1, "Mühendislik"),
            QuizQuestion("muh8", "Watt birimi neyi ölçer?", listOf("Enerji", "Güç", "Kuvvet", "Hız"), 1, "Mühendislik"),
            QuizQuestion("muh9", "Pascal birimi neyi ölçer?", listOf("Güç", "Basınç", "Enerji", "Hız"), 1, "Mühendislik"),
            QuizQuestion("muh10", "Tolerans mühendislikte ne demektir?", listOf("Sabır", "İzin verilen ölçü sapması", "Hata", "Kalite"), 1, "Mühendislik")
        ),
        
        "Tıp" to listOf(
            QuizQuestion("tip1", "Kalbin kaç odacığı vardır?", listOf("2", "3", "4", "5"), 2, "Tıp"),
            QuizQuestion("tip2", "Hemoglobin ne taşır?", listOf("Su", "Oksijen", "Glikoz", "Protein"), 1, "Tıp"),
            QuizQuestion("tip3", "Antibiyotikler neye karşı etkilidir?", listOf("Virüs", "Bakteri", "Mantar", "Parazit"), 1, "Tıp"),
            QuizQuestion("tip4", "Tansiyon ne ölçer?", listOf("Kalp hızı", "Kan basıncı", "Şeker", "Ateş"), 1, "Tıp"),
            QuizQuestion("tip5", "İnsülin hangi organdan salgılanır?", listOf("Karaciğer", "Pankreas", "Böbrek", "Mide"), 1, "Tıp"),
            QuizQuestion("tip6", "EKG ne ölçer?", listOf("Beyin dalgaları", "Kalbin elektriksel aktivitesi", "Kan şekeri", "Tansiyon"), 1, "Tıp"),
            QuizQuestion("tip7", "Aşılar nasıl çalışır?", listOf("Hastalığı tedavi eder", "Bağışıklık sistemini uyarır", "Ağrı keser", "Virüs öldürür"), 1, "Tıp"),
            QuizQuestion("tip8", "CPR (KPR) ne demektir?", listOf("Kan testi", "Kardiyopulmoner resüsitasyon", "Ameliyat", "İlaç"), 1, "Tıp"),
            QuizQuestion("tip9", "MR ne görüntüler?", listOf("Sadece kemik", "Yumuşak doku ve organlar", "Sadece kan", "Sadece beyin"), 1, "Tıp"),
            QuizQuestion("tip10", "Hipertansiyon ne demektir?", listOf("Düşük tansiyon", "Yüksek tansiyon", "Düşük şeker", "Yüksek şeker"), 1, "Tıp")
        ),
        
        "Hukuk" to listOf(
            QuizQuestion("huk1", "Anayasa ne demektir?", listOf("Ceza kanunu", "Devletin temel kanunu", "Ticaret kanunu", "Medeni kanun"), 1, "Hukuk"),
            QuizQuestion("huk2", "Kuvvetler ayrılığı ilkesinde kaç kuvvet vardır?", listOf("2", "3", "4", "5"), 1, "Hukuk"),
            QuizQuestion("huk3", "Yasama organı ne yapar?", listOf("Yargılama", "Kanun yapma", "Yürütme", "Denetleme"), 1, "Hukuk"),
            QuizQuestion("huk4", "Yargı bağımsızlığı ne demektir?", listOf("Yargıçların zenginliği", "Yargının diğer güçlerden bağımsız olması", "Mahkeme binası", "Avukat sayısı"), 1, "Hukuk"),
            QuizQuestion("huk5", "Ceza hukukunda 'masumiyet karinesi' ne demektir?", listOf("Herkes suçlu", "Suçu kanıtlanana kadar masum", "Herkes masum", "Ceza yok"), 1, "Hukuk"),
            QuizQuestion("huk6", "Medeni hukuk neyi düzenler?", listOf("Suçlar", "Kişiler arası özel ilişkiler", "Devlet", "Ceza"), 1, "Hukuk"),
            QuizQuestion("huk7", "Sözleşme hukuku hangi hukuk dalına girer?", listOf("Ceza", "Borçlar/Özel hukuk", "Anayasa", "İdare"), 1, "Hukuk"),
            QuizQuestion("huk8", "Türkiye'de en yüksek yargı organı hangisidir?", listOf("Asliye Mahkemesi", "Anayasa Mahkemesi", "Sulh Mahkemesi", "İdare Mahkemesi"), 1, "Hukuk"),
            QuizQuestion("huk9", "Hukuki ehliyet ne demektir?", listOf("Avukatlık", "Hak ve borç sahibi olabilme yeteneği", "Suç işleme", "Ceza alma"), 1, "Hukuk"),
            QuizQuestion("huk10", "Zamanaşımı ne demektir?", listOf("Zaman yönetimi", "Belirli süre sonra dava hakkının düşmesi", "Hızlı yargılama", "Erken karar"), 1, "Hukuk")
        )
    )
    
    /**
     * Belirli bir yetenek için tüm soruları döndürür
     */
    fun getQuestionsForSkill(skillName: String): List<QuizQuestion> {
        return allQuestions[skillName] ?: emptyList()
    }
    
    /**
     * Belirli bir yetenek için rastgele 5 soru seçer
     */
    fun getRandomQuestionsForQuiz(skillName: String): List<QuizQuestion> {
        val questions = getQuestionsForSkill(skillName)
        return if (questions.size >= QUESTIONS_PER_QUIZ) {
            questions.shuffled().take(QUESTIONS_PER_QUIZ)
        } else {
            questions.shuffled()
        }
    }
    
    /**
     * Quiz'in geçilip geçilmediğini kontrol eder
     */
    fun isQuizPassed(correctAnswers: Int): Boolean {
        return correctAnswers >= PASS_THRESHOLD
    }
    
    /**
     * Sorusu olan yeteneklerin listesini döndürür
     */
    fun getSkillsWithQuiz(): List<String> {
        return allQuestions.keys.toList()
    }
    
    /**
     * Bir yeteneğin quiz'i olup olmadığını kontrol eder
     */
    fun hasQuizForSkill(skillName: String): Boolean {
        return allQuestions.containsKey(skillName) && (allQuestions[skillName]?.isNotEmpty() == true)
    }
}

