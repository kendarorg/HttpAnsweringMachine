### Replace the click:

Regexp

FROM: [\t ]+(driver.findElement)([\(a-zA-Z0-9\.\":\-\ )]+)(.click\(\))
TO: doClick(()->$1$1)

### Scroll

scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")))

### Checkbox

CHECK: checkCheckBox(driver,() -> driver.findElement(By.id("force")));
UNCHECK: uncheckCheckBox(driver,() -> driver.findElement(By.id("force")));