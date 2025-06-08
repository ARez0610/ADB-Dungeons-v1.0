const puppeteer = require("puppeteer");
const fs = require("fs");
const path = require("path");

(async () => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();

  const htmlFiles = fs.readdirSync(".").filter(f => f.endsWith(".html")).sort();

  for (const file of htmlFiles) {
    const fullPath = "file://" + path.resolve(file);
    const outputName = file.replace(".html", ".pdf");

    console.log("Converting:", file);
    await page.goto(fullPath, { waitUntil: "networkidle0" });
    await page.pdf({
      path: outputName,
      format: "A4",
      printBackground: true
    });
  }

  await browser.close();
})();