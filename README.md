# **RoofedMaker**

This plugin helps you making a roofed forest with the length you want

**Steps to use:**

- Download latest release    [here](https://github.com/MonkeyDevelopment/RoofedMaker/releases)
- Drag file just downloaded in the `/plugins/` directory of your spigot server.
- Start server

**Configuration**
|Default Settings|Value|
|--|--|
|Clearing radius|300|
|Forest radius|200|
|Chunks to plant per tick\*|`5`|
|Chunks to clear per tick\*|`3`|
|Chunks to index per tick\*|`40`|
|World Name\*|`world`| 
|Debug Mode\*|`false`| 

 - Settings followed by a \* are manageable in the config.yml of the
   plugin.

**Generation**

To start the roofed forest generation, run the following command:

> /forestgen \<forest radius\>

Forest radius is *optional*, but if given the clearing radius will be equal to te `<forest radius> + 50`; For example if the forest radius is `350` then the clearing radius will be `400`

 - **If you enjoyed the plugin you can leave a star ⭐️**
