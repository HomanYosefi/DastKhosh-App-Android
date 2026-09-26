<div align="center">

# 💚 دست‌خوش | DastKhosh

### هزینه‌هات رو ساده ثبت کن، آگاهانه‌تر خرج کن.

اپلیکیشن فارسی مدیریت هزینه‌های شخصی، ساخته‌شده با Kotlin و Jetpack Compose  
با رابط کاربری راست‌به‌چپ و تمرکز بر تجربه‌ای ساده و روان.

<br />

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-16A34A?style=for-the-badge)
![MVVM](https://img.shields.io/badge/Pattern-MVVM-111827?style=for-the-badge)
![Hilt](https://img.shields.io/badge/DI-Hilt-F59E0B?style=for-the-badge)
![Room](https://img.shields.io/badge/Database-Room-0284C7?style=for-the-badge)

<br />

[معرفی](#about) ·
[قابلیت‌ها](#features) ·
[تصاویر](#screenshots) ·
[فناوری‌ها](#tech-stack) ·
[معماری](#architecture) ·
[اجرا](#getting-started) ·
[نقشه راه](#roadmap)

</div>

---

<a id="about"></a>

## 🌱 درباره دست‌خوش

**دست‌خوش** برای ثبت و مرور هزینه‌های روزمره طراحی شده است؛ بدون فرم‌های شلوغ و مراحل اضافه.

مبلغ را وارد کن، توضیحش را بنویس و هزینه را ذخیره کن. با مرور تاریخچه هم راحت‌تر ببین پولت کجا خرج شده است.

<a id="features"></a>

## ✨ قابلیت‌ها

### 📝 ثبت هزینه
- ثبت مبلغ و توضیحات هزینه.
- نمایش خوانای مبلغ به تومان.
- مدیریت وضعیت فرم و پیام‌های موفقیت و خطا.

### 🧾 مرور هزینه‌ها
- نمایش تاریخچه هزینه‌های ثبت‌شده.
- دسترسی به مبلغ و توضیحات هر هزینه در فهرست.
- نمایش تاریخ‌ها بر اساس تقویم شمسی.
- فیلتر هزینه‌ها در بازه‌های روزانه، هفتگی، ماهانه و سالانه.

### 💾 ذخیره‌سازی محلی
- ذخیره هزینه‌ها در پایگاه داده محلی با **Room**.
- ثبت و مرور هزینه‌های ذخیره‌شده بدون نیاز به اینترنت.

### 🎨 رابط کاربری
- طراحی با **Jetpack Compose**.
- پشتیبانی از چیدمان **راست‌به‌چپ**.
- نمایش ارقام فارسی و تفکیک سه‌رقمی مبالغ.
- استفاده از رنگ‌ها و سبک بصری یکپارچه در صفحات برنامه.

<a id="screenshots"></a>

## 📱 نگاهی به برنامه

<div align="center">

<table>
  <tr>
    <th>ثبت هزینه</th>
    <th>تاریخچه هزینه‌ها</th>
    <th>نمودار هزینه‌ها</th>
  </tr>
  <tr>
    <td><img src="pics/Screenshot%201405-07-04%20at%203.58.40%E2%80%AFPM.png" width="250" alt="صفحه ثبت هزینه در دست‌خوش اندروید" /></td>
    <td><img src="pics/Screenshot%201405-07-04%20at%203.59.31%E2%80%AFPM.png" width="250" alt="صفحه تاریخچه هزینه‌ها در دست‌خوش اندروید" /></td>
    <td><img src="pics/Screenshot%201405-07-04%20at%203.59.55%E2%80%AFPM.png" width="250" alt="صفحه نمودار هزینه‌ها در دست‌خوش اندروید" /></td>
  </tr>
</table>

</div>

<a id="tech-stack"></a>

## 🛠 فناوری‌ها و ابزارها

| بخش | فناوری |
| :--- | :--- |
| زبان برنامه‌نویسی | Kotlin |
| رابط کاربری | Jetpack Compose |
| معماری | Clean Architecture |
| الگوی لایه نمایش | MVVM |
| تزریق وابستگی | Hilt |
| پایگاه داده محلی | Room |
| محیط توسعه | Android Studio |
| سیستم ساخت و مدیریت وابستگی‌ها | Gradle |

### چرا این ترکیب؟

- **Kotlin:** برای توسعه اندروید با کدی خوانا و قابلیت‌های مدرن زبان.
- **Jetpack Compose:** برای ساخت رابط کاربری اعلانی و مبتنی بر وضعیت.
- **Clean Architecture:** برای جداسازی مسئولیت‌ها و کاهش وابستگی منطق برنامه به ابزارها.
- **MVVM:** برای جداسازی منطق نمایش و مدیریت وضعیت از رابط کاربری.
- **Hilt:** برای تأمین وابستگی‌ها و مدیریت چرخه عمر آن‌ها.
- **Room:** برای ذخیره‌سازی ساختاریافته هزینه‌ها در پایگاه داده محلی.

<a id="architecture"></a>

## 🧩 معماری پروژه

معماری برنامه بر پایه **Clean Architecture** و الگوی **MVVM** شکل گرفته است. مسئولیت‌ها در سه لایه اصلی تفکیک می‌شوند:

### 🎨 Presentation — لایه نمایش

شامل صفحه‌های **Compose**، کامپوننت‌های رابط کاربری و **ViewModel**‌ها.

- نمایش اطلاعات و دریافت تعاملات کاربر.
- نگهداری و مدیریت وضعیت صفحه.
- فراخوانی Use Caseها برای اجرای عملیات.
- نمایش وضعیت‌های بارگذاری، موفقیت و خطا.

### 🧠 Domain — لایه منطق برنامه

شامل مدل‌های دامنه، قرارداد Repositoryها و **Use Case**ها.

- تعریف عملیات برنامه، مانند ثبت و دریافت هزینه‌ها.
- نگهداری قوانین و منطق اصلی برنامه.
- استقلال از جزئیات رابط کاربری و پایگاه داده.

### 💾 Data — لایه داده

شامل پیاده‌سازی Repositoryها و اجزای پایگاه داده **Room**.

- ذخیره و بازیابی هزینه‌ها.
- تعریف Entityها، DAOها و Database.
- تبدیل داده‌های ذخیره‌سازی به مدل‌های دامنه و برعکس.
- پیاده‌سازی قراردادهای تعریف‌شده در لایه Domain.

### جهت وابستگی‌ها

```text
Presentation ───────► Domain ◄─────── Data
Compose + ViewModel  Use Cases       Repository Implementation
                     Models          Room / DAO / Entity
                     Repository
                     Interfaces
```

**لایه Domain به Presentation یا Data وابسته نیست.**  
لایه Data قراردادهای Domain را پیاده‌سازی می‌کند و **Hilt** مسئول اتصال وابستگی‌ها هنگام اجرای برنامه است.

<a id="getting-started"></a>

## 🚀 اجرای پروژه

### پیش‌نیازها

- نسخه سازگار **Android Studio** با تنظیمات پروژه.
- **Android SDK** متناسب با `compileSdk` پروژه.
- نسخه **JDK** سازگار با Android Gradle Plugin پروژه.
- شبیه‌ساز یا دستگاه اندرویدی با نسخه برابر یا بالاتر از `minSdk` پروژه.

### ۱. دریافت سورس

```bash
git clone https://github.com/HomanYosefi/DastKhosh-App-Android
cd DastKhosh
```

> مقدار `YOUR_USERNAME` و در صورت نیاز نام مخزن را با اطلاعات مخزن خودت جایگزین کن.

### ۲. بازکردن پروژه

در **Android Studio** گزینه **Open** را انتخاب کن و پوشه اصلی پروژه را باز کن؛ همان پوشه‌ای که فایل `settings.gradle` یا `settings.gradle.kts` در آن قرار دارد.

### ۳. دریافت وابستگی‌ها

اجازه بده **Gradle Sync** کامل شود. وابستگی‌های تعریف‌شده در پروژه، از جمله موارد زیر، توسط Gradle دریافت می‌شوند:

- [Jetpack Compose](https://developer.android.com/compose)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room](https://developer.android.com/training/data-storage/room)

اگر همگام‌سازی خودکار شروع نشد، از منوی Android Studio استفاده کن:

```text
File → Sync Project with Gradle Files
```

> برای اولین همگام‌سازی، دانلود SDK و دریافت وابستگی‌ها به اینترنت نیاز داری.

### ۴. اجرا

یک شبیه‌ساز یا دستگاه متصل انتخاب کن و روی **Run ▶** بزن.

> برای اجرا روی گوشی واقعی، **Developer options** و **USB debugging** را فعال کن و مجوز اتصال به کامپیوتر را تأیید کن.

### ۵. ساخت APK آزمایشی

در macOS یا Linux:

```bash
./gradlew assembleDebug
```

در Windows:

```powershell
.\gradlew.bat assembleDebug
```

در ساختار استاندارد با ماژول `app` و بدون Product Flavor، فایل خروجی معمولاً در مسیر زیر قرار می‌گیرد:

```text
app/build/outputs/apk/debug/app-debug.apk
```

<a id="roadmap"></a>

## 🧭 نقشه راه

قابلیت‌هایی که برای ادامه توسعه در نظر گرفته شده‌اند:

- [ ] جست‌وجوی هزینه‌ها بر اساس توضیحات.
- [ ] انتخاب بازه زمانی دلخواه.
- [ ] نمایش خلاصه مالی، مجموع هزینه‌ها و بیشترین هزینه.
- [ ] مقایسه هزینه‌ها با دوره قبل به‌صورت درصد و نمودار.
- [ ] گسترش نمودارها و گزارش‌های تحلیلی.
- [ ] ارائه نکات مالی مبتنی بر قوانین و الگوی هزینه‌ها.

<a id="contributing"></a>

## 🤝 مشارکت

پیشنهادها، گزارش باگ‌ها و بهبودهای شما ارزشمند هستند.

برای مشارکت:

1. یک **Issue** برای توضیح مشکل یا پیشنهادت باز کن.
2. پروژه را **Fork** کن.
3. تغییرات را در یک شاخه مستقل انجام بده.
4. یک **Pull Request** با توضیح روشن ارسال کن.

---

<div align="center">

### 💚 هر هزینه، یک قدم به شناخت عادت‌های مالی

اگر دست‌خوش برات جالب بود، با یک ⭐ از پروژه حمایت کن.

**ساخته‌شده با Kotlin و Jetpack Compose، با توجه به جزئیات**

</div>
