package com.example.firstapp.shared

object SharedData {
    var sharedUnit: String = "metric"
    var sharedLatitude : Double = 21.0285
    var sharedLongitude: Double = 105.8542
    var sharedCity: String = "Hanoi"
    
    // Biến để đánh dấu người dùng vừa thực hiện tìm kiếm tạm thời
    var isTemporarySearch: Boolean = false
}
