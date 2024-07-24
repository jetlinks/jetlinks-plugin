

test("test Plugin Driver", async () => {


    let driver =
        await import ("./TestPluginDriver");

    let instance = new driver.default();

    expect(instance).not.toEqual(null);

    console.log(instance.getType())

})