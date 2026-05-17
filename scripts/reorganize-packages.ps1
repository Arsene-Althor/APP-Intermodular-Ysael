$ErrorActionPreference = "Stop"
$base = "c:\Users\Almon\Documents\Proyecto Individual\APP-Intermodular-Ysael\app\src\main\java\com\example\hotel_pere_maria_app"

$moves = [ordered]@{
    "ui\MainActivity.kt" = "MainActivity.kt"
    "ui\Navegation\Routes.kt" = "core\navigation\Routes.kt"
    "ui\Navegation\Navegation.kt" = "core\navigation\AppNavGraph.kt"
    "ui\Navegation\NavegationMain.kt" = "core\navigation\AppNavHost.kt"
    "ui\Navegation\NavigationExtensions.kt" = "core\navigation\NavigationExtensions.kt"
    "ui\Service\RetrofitClient.kt" = "core\network\RetrofitClient.kt"
    "ui\Service\AuthService.kt" = "core\network\AuthService.kt"
    "ui\Service\ReservationService.kt" = "core\network\ReservationService.kt"
    "ui\Service\RoomService.kt" = "core\network\RoomService.kt"
    "ui\Service\ReviewService.kt" = "core\network\ReviewService.kt"
    "ui\Service\UserService.kt" = "core\network\UserService.kt"
    "ui\Service\LoyaltyStatsService.kt" = "core\network\LoyaltyStatsService.kt"
    "ui\Service\UserStayService.kt" = "core\network\UserStayService.kt"
    "ui\Service\FlexibilityService.kt" = "core\network\FlexibilityService.kt"
    "ui\Service\SessionManager.kt" = "core\session\SessionManager.kt"
    "ui\Service\SessionUi.kt" = "core\session\SessionUi.kt"
    "ui\Service\ThemeManager.kt" = "core\session\ThemeManager.kt"
    "ui\Service\MediaUrls.kt" = "core\util\MediaUrls.kt"
    "ui\Service\ApiMessages.kt" = "core\util\ApiMessages.kt"
    "ui\Service\InvoicePdfHelper.kt" = "core\util\InvoicePdfHelper.kt"
    "ui\Service\FlexibilityNotificationHelper.kt" = "feature\flexibility\FlexibilityNotificationHelper.kt"
    "ui\Service\FlexibilityPollWorker.kt" = "feature\flexibility\FlexibilityPollWorker.kt"
    "ui\Models\RoomRepository.kt" = "data\repository\RoomRepository.kt"
    "ui\Models\Reservation.kt" = "data\model\Reservation.kt"
    # ReservationRepository.kt already at data\repository\
    "ui\Models\Room.kt" = "data\model\Room.kt"
    "ui\Models\User.kt" = "data\model\User.kt"
    "ui\Models\Review.kt" = "data\model\Review.kt"
    "ui\Models\LoginRequest.kt" = "data\model\LoginRequest.kt"
    "ui\Models\LoginResponse.kt" = "data\model\LoginResponse.kt"
    "ui\Models\RegisterRequest.kt" = "data\model\RegisterRequest.kt"
    "ui\Models\RegisterResponse.kt" = "data\model\RegisterResponse.kt"
    "ui\Models\BookingAuditEntry.kt" = "data\model\BookingAuditEntry.kt"
    "ui\Models\BookingHistoryFriendlyMapper.kt" = "data\model\BookingHistoryFriendlyMapper.kt"
    "ui\Models\ClientLoyaltyStats.kt" = "data\model\ClientLoyaltyStats.kt"
    "ui\Models\ExtendStayResponse.kt" = "data\model\ExtendStayResponse.kt"
    "ui\Models\ExtraService.kt" = "data\model\ExtraService.kt"
    "ui\Models\FlexibilityModels.kt" = "data\model\FlexibilityModels.kt"
    "ui\Models\FlexibilityRepository.kt" = "data\repository\FlexibilityRepository.kt"
    "ui\Models\LoyaltyFeedback.kt" = "data\model\LoyaltyFeedback.kt"
    "ui\Models\LoyaltyStatsRepository.kt" = "data\repository\LoyaltyStatsRepository.kt"
    "ui\Models\UserStayModels.kt" = "data\model\UserStayModels.kt"
    "ui\Models\UserStayRepository.kt" = "data\repository\UserStayRepository.kt"
    "ui\Scaffold\BottomBookingBar.kt" = "ui\scaffold\BottomBookingBar.kt"
    "ui\Scaffold\ScaffoldMain.kt" = "ui\scaffold\ScaffoldMain.kt"
    "ui\Scaffold\TopAppBar.kt" = "ui\scaffold\TopAppBar.kt"
    "ui\Views\Components.kt" = "ui\components\DateInputs.kt"
    "ui\booking\BookingSearchSession.kt" = "feature\booking\BookingSearchSession.kt"
    "ui\booking\BookingHomeScreen.kt" = "feature\booking\BookingHomeScreen.kt"
    "ui\booking\BookingResultsScreen.kt" = "feature\booking\BookingResultsScreen.kt"
    "ui\booking\BookingConfirmScreen.kt" = "feature\booking\BookingConfirmScreen.kt"
    "ui\ViewModels\HomeViewModel.kt" = "feature\booking\_delete_HomeViewModel.kt"
    "ui\Views\Login.kt" = "feature\auth\LoginScreen.kt"
    "ui\Views\Register.kt" = "feature\auth\RegisterScreen.kt"
    "ui\Views\ForgotPassword.kt" = "feature\auth\ForgotPasswordScreen.kt"
    "ui\ViewModels\LoginViewModel.kt" = "feature\auth\LoginViewModel.kt"
    "ui\ViewModels\RegisterViewModel.kt" = "feature\auth\RegisterViewModel.kt"
    "ui\ViewModels\ForgotPasswordViewModel.kt" = "feature\auth\ForgotPasswordViewModel.kt"
    "ui\Views\MyBookingsScreens.kt" = "feature\reservation\MyBookingsScreens.kt"
    "ui\Views\ModReserva.kt" = "feature\reservation\ModReservaScreen.kt"
    "ui\Views\ReservationAuditScreen.kt" = "feature\reservation\ReservationAuditScreen.kt"
    "ui\ViewModels\ModReservaViewModel.kt" = "feature\reservation\ModReservaViewModel.kt"
    "ui\ViewModels\ReservationAuditViewModel.kt" = "feature\reservation\ReservationAuditViewModel.kt"
    "ui\Views\FlexibilityUi.kt" = "feature\flexibility\FlexibilityUi.kt"
    "ui\Views\ClientStatsScreen.kt" = "feature\loyalty\ClientStatsScreen.kt"
    "ui\Views\MyStaysScreen.kt" = "feature\loyalty\MyStaysScreen.kt"
    "ui\Views\StayDetailScreen.kt" = "feature\loyalty\StayDetailScreen.kt"
    "ui\Views\InvoiceHistoryScreen.kt" = "feature\invoice\InvoiceHistoryScreen.kt"
    "ui\Views\Profile.kt" = "feature\profile\ProfileScreen.kt"
    "ui\ViewModels\ProfileViewModel.kt" = "feature\profile\ProfileViewModel.kt"
    "ui\Views\Reviews.kt" = "feature\review\ReviewsScreen.kt"
    "ui\ViewModels\ReviewViewModel.kt" = "feature\review\ReviewViewModel.kt"
    "ui\ViewModels\MyReviewsViewModel.kt" = "feature\review\MyReviewsViewModel.kt"
    "ui\Views\RoomDetail.kt" = "feature\room\RoomDetailScreen.kt"
    "ui\ViewModels\RoomViewModel.kt" = "feature\room\RoomViewModel.kt"
}

foreach ($entry in $moves.GetEnumerator()) {
    $src = Join-Path $base $entry.Key
    $dst = Join-Path $base $entry.Value
    if (-not (Test-Path $src)) { continue }
    $dir = Split-Path $dst -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    Move-Item -Path $src -Destination $dst -Force
}

# Remove old HomeViewModel if staged for delete
$del = Join-Path $base "feature\booking\_delete_HomeViewModel.kt"
if (Test-Path $del) { Remove-Item $del -Force }

function Package-From-Path($relPath) {
    $dir = Split-Path $relPath -Parent
    if ([string]::IsNullOrEmpty($dir)) {
        return "com.example.hotel_pere_maria_app"
    }
    $pkg = $dir -replace '\\', '.'
    return "com.example.hotel_pere_maria_app.$pkg"
}

Get-ChildItem -Path $base -Recurse -Filter "*.kt" | ForEach-Object {
    $rel = $_.FullName.Substring($base.Length + 1)
    $pkg = Package-From-Path $rel
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    if ($content -match '(?m)^package\s+[\w.]+') {
        $content = $content -replace '(?m)^package\s+[\w.]+', "package $pkg"
    } else {
        $content = "package $pkg`n`n$content"
    }
    Set-Content -Path $_.FullName -Value $content.TrimEnd() -Encoding UTF8 -NoNewline
    Add-Content -Path $_.FullName -Value "`n" -Encoding UTF8
}

$replacements = [ordered]@{
    'com.example.hotel_pere_maria_app.ui.Navegation.' = 'com.example.hotel_pere_maria_app.core.navigation.'
    'com.example.hotel_pere_maria_app.ui.Scaffold.' = 'com.example.hotel_pere_maria_app.ui.scaffold.'
    'com.example.hotel_pere_maria_app.ui.Service.SessionManager' = 'com.example.hotel_pere_maria_app.core.session.SessionManager'
    'com.example.hotel_pere_maria_app.ui.Service.SessionUi' = 'com.example.hotel_pere_maria_app.core.session.SessionUi'
    'com.example.hotel_pere_maria_app.ui.Service.ThemeManager' = 'com.example.hotel_pere_maria_app.core.session.ThemeManager'
    'com.example.hotel_pere_maria_app.ui.Service.MediaUrls' = 'com.example.hotel_pere_maria_app.core.util.MediaUrls'
    'com.example.hotel_pere_maria_app.ui.Service.ApiMessages' = 'com.example.hotel_pere_maria_app.core.util.ApiMessages'
    'com.example.hotel_pere_maria_app.ui.Service.InvoicePdfHelper' = 'com.example.hotel_pere_maria_app.core.util.InvoicePdfHelper'
    'com.example.hotel_pere_maria_app.ui.Service.FlexibilityNotificationHelper' = 'com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityNotificationHelper'
    'com.example.hotel_pere_maria_app.ui.Service.FlexibilityPollWorker' = 'com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityPollWorker'
    'com.example.hotel_pere_maria_app.ui.Service.' = 'com.example.hotel_pere_maria_app.core.network.'
    'com.example.hotel_pere_maria_app.ui.Models.RoomRepository' = 'com.example.hotel_pere_maria_app.data.repository.RoomRepository'
    'com.example.hotel_pere_maria_app.ui.Models.FlexibilityRepository' = 'com.example.hotel_pere_maria_app.data.repository.FlexibilityRepository'
    'com.example.hotel_pere_maria_app.ui.Models.LoyaltyStatsRepository' = 'com.example.hotel_pere_maria_app.data.repository.LoyaltyStatsRepository'
    'com.example.hotel_pere_maria_app.ui.Models.UserStayRepository' = 'com.example.hotel_pere_maria_app.data.repository.UserStayRepository'
    'com.example.hotel_pere_maria_app.ui.Models.ReservationRepository' = 'com.example.hotel_pere_maria_app.data.repository.ReservationRepository'
    'com.example.hotel_pere_maria_app.ui.Models.' = 'com.example.hotel_pere_maria_app.data.model.'
    'com.example.hotel_pere_maria_app.ui.booking.' = 'com.example.hotel_pere_maria_app.feature.booking.'
    'com.example.hotel_pere_maria_app.ui.ViewModels.HomeViewModel' = 'com.example.hotel_pere_maria_app.feature.booking.BookingHomeViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.LoginViewModel' = 'com.example.hotel_pere_maria_app.feature.auth.LoginViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.RegisterViewModel' = 'com.example.hotel_pere_maria_app.feature.auth.RegisterViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.ForgotPasswordViewModel' = 'com.example.hotel_pere_maria_app.feature.auth.ForgotPasswordViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.ModReservaViewModel' = 'com.example.hotel_pere_maria_app.feature.reservation.ModReservaViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.ReservationAuditViewModel' = 'com.example.hotel_pere_maria_app.feature.reservation.ReservationAuditViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.ProfileViewModel' = 'com.example.hotel_pere_maria_app.feature.profile.ProfileViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.ReviewViewModel' = 'com.example.hotel_pere_maria_app.feature.review.ReviewViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.MyReviewsViewModel' = 'com.example.hotel_pere_maria_app.feature.review.MyReviewsViewModel'
    'com.example.hotel_pere_maria_app.ui.ViewModels.RoomViewModel' = 'com.example.hotel_pere_maria_app.feature.room.RoomViewModel'
    'com.example.hotel_pere_maria_app.ui.Views.Login' = 'com.example.hotel_pere_maria_app.feature.auth.LoginScreen'
    'com.example.hotel_pere_maria_app.ui.Views.Register' = 'com.example.hotel_pere_maria_app.feature.auth.RegisterScreen'
    'com.example.hotel_pere_maria_app.ui.Views.ForgotPassword' = 'com.example.hotel_pere_maria_app.feature.auth.ForgotPasswordScreen'
    'com.example.hotel_pere_maria_app.ui.Views.MyBookingsScreens' = 'com.example.hotel_pere_maria_app.feature.reservation.MyBookingsScreens'
    'com.example.hotel_pere_maria_app.ui.Views.ModReserva' = 'com.example.hotel_pere_maria_app.feature.reservation.ModReservaScreen'
    'com.example.hotel_pere_maria_app.ui.Views.ReservationAuditScreen' = 'com.example.hotel_pere_maria_app.feature.reservation.ReservationAuditScreen'
    'com.example.hotel_pere_maria_app.ui.Views.FlexibilityUi' = 'com.example.hotel_pere_maria_app.feature.flexibility.FlexibilityUi'
    'com.example.hotel_pere_maria_app.ui.Views.ClientStatsScreen' = 'com.example.hotel_pere_maria_app.feature.loyalty.ClientStatsScreen'
    'com.example.hotel_pere_maria_app.ui.Views.MyStaysScreen' = 'com.example.hotel_pere_maria_app.feature.loyalty.MyStaysScreen'
    'com.example.hotel_pere_maria_app.ui.Views.StayDetailScreen' = 'com.example.hotel_pere_maria_app.feature.loyalty.StayDetailScreen'
    'com.example.hotel_pere_maria_app.ui.Views.InvoiceHistoryScreen' = 'com.example.hotel_pere_maria_app.feature.invoice.InvoiceHistoryScreen'
    'com.example.hotel_pere_maria_app.ui.Views.Profile' = 'com.example.hotel_pere_maria_app.feature.profile.ProfileScreen'
    'com.example.hotel_pere_maria_app.ui.Views.Reviews' = 'com.example.hotel_pere_maria_app.feature.review.ReviewsScreen'
    'com.example.hotel_pere_maria_app.ui.Views.RoomDetail' = 'com.example.hotel_pere_maria_app.feature.room.RoomDetailScreen'
    'com.example.hotel_pere_maria_app.ui.Views.FechaInputSimple' = 'com.example.hotel_pere_maria_app.ui.components.FechaInputSimple'
    'com.example.hotel_pere_maria_app.ui.MainActivity' = 'com.example.hotel_pere_maria_app.MainActivity'
}

Get-ChildItem -Path $base -Recurse -Filter "*.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    foreach ($pair in $replacements.GetEnumerator()) {
        $content = $content.Replace($pair.Key, $pair.Value)
    }
    Set-Content -Path $_.FullName -Value $content.TrimEnd() -Encoding UTF8 -NoNewline
    Add-Content -Path $_.FullName -Value "`n" -Encoding UTF8
}

Write-Host "Reorganization complete."
