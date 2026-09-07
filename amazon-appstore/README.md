# Amazon Appstore Publishing Kit - Mind Notepad

This folder contains all the assets, texts, metadata, icons, and instructions needed to publish **Mind Notepad** to the Amazon Appstore.

---

## 📁 Folder Structure

```text
amazon-appstore/
├── icons/
│   ├── icon_512x512.png         <-- Large App Icon (512x512 px PNG)
│   └── icon_114x114.png         <-- Small App Icon (114x114 px PNG)
├── screenshots/
│   ├── screenshot_1_home_dashboard.png    <-- Screen 1: Dashboard (1080x1920 RGB PNG)
│   ├── screenshot_2_rich_text_editor.png  <-- Screen 2: Editor (1080x1920 RGB PNG)
│   └── screenshot_3_dark_mode.png         <-- Screen 3: Dark Mode (1080x1920 RGB PNG)
├── AMAZON_APPSTORE_LISTING.md    <-- Complete Title, Descriptions, Bullets, Keywords
├── SCREENSHOTS_GUIDE.md          <-- Screenshot dimensions & details
├── privacy_policy.html           <-- Ready-to-host Privacy Policy for GitHub Pages
└── README.md                     <-- Quick overview and checklist
```

---

## 🚀 Quick Step-by-Step Submission Checklist

1. [ ] Log in to [Amazon Developer Console](https://developer.amazon.com/apps-and-games).
2. [ ] Click **Add New App** -> Select **Android**.
3. [ ] Fill in **General Information** from `AMAZON_APPSTORE_LISTING.md`.
4. [ ] Set **Availability & Pricing** to `Free` and select `All countries`.
5. [ ] Copy & paste **Short Description**, **Long Description**, **Feature Bullets**, and **Keywords** from `AMAZON_APPSTORE_LISTING.md`.
6. [ ] Upload icons from the `icons/` folder:
   - Large Icon: `icons/icon_512x512.png`
   - Small Icon: `icons/icon_114x114.png`
7. [ ] Capture and upload **3 Screenshots** as instructed in `SCREENSHOTS_GUIDE.md`.
8. [ ] In **Content Rating**, mark `Does this app contain ads?` as **YES**.
9. [ ] Under **Privacy Policy URL**, paste the URL of your hosted `privacy_policy.html` (e.g. on GitHub Pages).
10. [ ] In **Appstore Details**, upload your signed release APK built via GitHub Actions.
11. [ ] Click **Submit App**!
