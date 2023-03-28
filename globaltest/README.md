### Replace the click:

Regexp

FROM: [\t ]+(driver.findElement)([\(a-zA-Z0-9\.\":\-\ )]+)(.click\(\))
TO: doClick(()->$1$2)

### Scroll

scrollFind(driver, () -> driver.findElement(By.id("grid-s-c-1")))

### Checkbox

CHECK: checkCheckBox(driver,() -> driver.findElement(By.id("force")));
UNCHECK: uncheckCheckBox(driver,() -> driver.findElement(By.id("force")));



sudo setcap CAP_NET_BIND_SERVICE=+eip /usr/lib/jvm/java-1.11.0-openjdk-amd64/bin/java
