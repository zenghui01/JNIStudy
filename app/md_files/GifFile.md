# GIF文件格式

---

## GIF(Graphics Interchange Format)文件
### 概述
GIF(Graphics Interchange Format，图形交换格式)文件是由 CompuServe公司开发的图形文件格式，版权所有，任何商业目的使用均须 CompuServe公司授权。

GIF图像是基于 **颜色列表** 的（存储的数据是该点的颜色对应于颜色列表的**索引值**），最多只支持8位（256色）。GIF文件内部分成许多**存储块**，用来存储多幅图像或者是决定图像表现行为的控制块，用以实现动画和交互式应用。GIF文件还通过LZW压缩算法压缩图像数据来减少图象尺寸。

> 官方资料：
http://giflib.sourceforge.net/index.html
https://www.w3.org/Graphics/GIF/spec-gif89a.txt

### GIF文件格式
<img src="\图片\gif_file_stream.gif" alt="gif_file_stream" style="zoom:150%;" />

共有十一块，其中有八个块是 GIF 图像必备的（实线边框），以及三个可选块（虚线边框）。

> 必备块：
>
>  - Header（文件头信息）
>  - Logical Screen Descriptor（逻辑屏幕描述符）
>  - Image Descriptor（图像描述符）
>  - Image Data（图像数据流）
>  - Plain Text Extension（文本扩展）
>  - Application Extension（应用扩展）
>  - Comment Extension（注释扩展）
>  - Trailer（尾部标记）

> 可选块：
>
>  - Global Color Table（全局颜色表）
>  - **Graphic Control Extension（图形控制扩展）**
>  - Local Color Table（本地颜色表）

#### Header

<img src="\图片\header_block.gif" alt="header_block" style="zoom:150%;" />
固定由6个字节组成。前三个字节是文件签名，后三个字节是版本号。

|字节|描述|
|-|-|
|1-3|Signature：文件签名。<br />47-'G'<br />49-'I'<br />46-'F'|
|4-6|Version：版本号。<br />38-'8'<br />39-'9'<br />61-'a'|

#### Logical Screen Descriptor

<img src="\图片\logical_screen_desc_block.gif" alt="logical_screen_desc_block" style="zoom:150%;" />
固定由7字节组成。

|字节|描述|
|:--|-|
| １-２ |Canvas Width：表示 GIF 图像的宽度，单位是像素。<br />如：0A00 = 10(十进制)，表示图像宽度为10像素。|
|3-4|Canvas Height：表示 GIF 图像的高度，单位是像素。<br />如：0A00 = 10(十进制)，表示图像高度为10像素。|
|5|Packed Field：这是一个包装字段，内部的不同 bit（位）表示有不同的含义。<br /><br />从左边数第一位表示 Global Color Table Flag，如果其为 1 ，则表示存在 Global Color Table。如果为 0，则没有 Global Color Table。<br /><br />从左边数第二、三、四位表示 Color Resolution，用于表示色彩分辨率，如果为 s，则 Global Color Table 的颜色数为 2^(s+1)个，如果这是 s = 1,则一共有 4 种颜色，即每个像素可以用 2位（二进制） 来表示。<br /><br />从左边数第五位表示 Sort Flag，它有两个值 0 或 1。如果为 0 则 Global Color Table 不进行排序，为 1 则表示 Global Color Table 按照降序排列，出现频率最多的颜色排在最前面。<br /><br />最右边三位表示 Global Color Table 的颜色数，如其值为 s，则全局列表颜色个数的计算公式为 2^(s+1)。如 s = 1，则 Global Color Table 包含 4 个颜色。|
|6|Background Color Index：表示 GIF 的背景色在 Global Color Table 中的索引。如果没有全局颜色列表，该值没有意义。|
|7|Pixel Aspect Ratio：表示用于计算原始图像中像素宽高比的近似因子，一般情况为 0。|

#### Global Color Table

<img src="\图片\global_color_table.gif" alt="global_color_table" style="zoom:150%;" />
如果有的话就会跟在 Logical Screen Descriptor 块后面。

在 Global Color Table 中每个字节仅代表一种颜色，所以 Global Color Table 的字节数 = 颜色数 * 3。在 Logical Screen Descriptor 中我们知道示例中包含 4 种颜色，即 Global Color Table 的字节数为 12 。所以读取接下来的 12 个字节。

#### **Graphics Control Extension**

<img src="\图片\graphic_control_ext.gif" alt="graphic_control_ext" style="zoom:150%;" />

|字节|描述|
|-|-|
|1|Extension Introducer：扩展入口，固定值为21，表示接下来是扩展块数据|
|2|Graphics Control Lable：图形控制扩展块标签，表示该扩展块是一个图形控制扩展块。在这里也是固定值F9。|
|3|Byte Size：表示接下来的有效数据字节数。|
|4|Packed Field：是一个包装字段，内部不同位的意义也不同。<br /><br />前三位表示Reserved for Future Use，即保留位，暂无用处。<br /><br />第四，五，六位表示 Disposal Method，表示在进行逐帧渲染时，前一帧留下的图像作何处理。<br /><br />倒数第二位表示 User Input Flag，表示是否需要在得到用户的输入时才进行下一帧的输入（具体用户输入指什么视应用而定，可以是按回车键、鼠标点击等），可以和延迟时间一起使用，在设置的延迟时间内用户有输入则马上继续进行，或者没有输入直到延迟时间到达而继续。<br />0：不做任何处理。<br />1：保留前一帧图像，在此基础上进行渲染。<br />2：渲染前将图像置为背景色。<br />3：将被前下一帧覆盖的图像重置。<br /><br />最后一位，表示 Transparent Flag。0 表示无需用户输入。1 表示需要用户输入。|
|5-6|Delay Time：表示 GIF 动图每一帧之间的间隔，单位为百分之一秒(10ms)。当为 0 时间隔由解码器管理。|
|7|Transparent Color Index：当 Transparent Flag 为 1 时，此字节有效，表示此索引在 Global Color Table 中对应的颜色将被当做透明色做处理|
|8|Block Terminator：表示 Extension 到此结束。固定值为 00。|

#### Image Descriptor

<img src="\图片\image_descriptor_block.gif" alt="image_descriptor_block" style="zoom:150%;" />

|字节|描述|
|-|-|
|1|Image Seperator：固定值为2C|
|2-3|Image Left：表示下一帧图像渲染位置离画布左边的距离（从 0 开始）。|
|4-5|Image Top：该值表示下一帧图像渲染位置离画布上边的距离（从 0 开始）。|
|6-7|Image Width：该值表示下一帧图像的宽度。|
|8-9|Image Height：该值表示下一帧图像的高度。|
|10|Packed Field：这是一个包装字段，内部不同位的意义也不同。<br /><br />第一位：Local Color Table Flag，表示下一帧图像是否需要一个独立的颜色表。1 为需要，0 为不需要。<br /><br />第二位：Interlace Flag，表示是否需要隔行扫描。1 为需要，0 为不需要。<br /><br />第三位：Sort Flag，如果需要 Local Color Table 的话，这个字段表示其排列顺序，同 Global Color Table。<br /><br />第四、五位：Reserved For Future Use，保留位。<br /><br />最后三位：Size of Local Color Table，同 Global Color Table 中的该位。如需要本地颜色表，则该数有效。|

> 我们不是在 Logical Screen Descriptor 中知道了图像的分辨率吗，为什么还要在 Image Descriptor 中额外指定图像的宽和高?

> 其实 GIF 在进行编码的时候并不一定对每一帧进行全尺寸的压缩。因为有时候一个 GIF 图只有中间区域是动的，四周都是静止的，那只需要对中间那部分进行压缩编码即可。所以这里的 Image Left、Image Top、Image Width 和 Image Height正好可以指定一个小于等于 GIF 分辨率的图像。

#### Local Color Table
如果Image Description中Local Color Table的标签值为1，跟在Image Description后面。与Global Color Table一样处理。

#### Image Data

<img src="\图片\image_data_block.gif" alt="image_data_block" style="zoom:150%;" />
如果存在 Local Color Table，Image Data 就紧跟其后。如若不存在，则紧跟在 Image Descriptor 后。

|字节|描述|
|-|-|
|1|LZW Minimum Code Size: GIF 在对每一帧的像素颜色在 Color Table 所对应的索引进行 LZW 压缩，这里的 LZW Minimum Code Size 就是 LZW 压缩中很关键的一个值|
|2|Number of bytes of data in sun-blocks（01-FF）：这个值表示在其后面的有效字节的个数。范围为 01-FF，当其值为 0，则表示 Image Data 到此为止，后面就是其他块的数据了。这里需要注意由于其最大值为 FF，但图像的像素个数可能会大于这个值，所以从图上也能知道这个 Data sub-Blocks是有可能接连出现很多个的。|
|n|Data Sub-Block(s)：表示有效的字节块。|
|最后一个字节|Block Terminator：表示 Image Data 的结束部分。|

#### Plain Text Extension
|字节|描述|
|-|-|
|1|Extension Introducer：扩展块标识，固定值为 21。|
|2|Plaint Text Lablel：图形文本扩展块标识，固定值为 01。|
|3|Block Size：块大小，固定值为 12。|
|4-5|Text Glid Left Position：文本框左边界位置，像素值，表示文本框据GIF图形左边界的距离|
|6-7|Text Glid Top Position：文本框上边界位置，像素值，表示文本框据GIF图形上边界的距离|
|8-9|Text Glid Width：文本框宽度，像素值。|
|10-11|Text Glid Height：文本框高度，像素值。|
|12|Character Cell Width：文本字符单元格宽度，像素值。|
|13|Character Cell Height：文本字符单元格高度，像素值。|
|14-15|Text Foreground Color Index：文本前景色在全局颜色列表中的索引。|
|n|Plain Text Data - 一个或多个数据块(Data Sub-Blocks)组成，保存要在显示的字符串。|
|最后一个字节|Block Terminator：文本扩展块结束标识，固定值为 0。|

#### Application Extension
|字节|描述|
|-|-|
|1|Extension Introducer：扩展块标识，固定值为 21。|
|2|Application Extension Label： 应用程序扩展块标识，固定值为 FF。|
|3|Length of Application Block：块大小，固定值为 11。|
|4-11|Application Identifier：应用程序标识符，8个连续的ASCII字符("NETSCAPE")。|
|12-14|Application Authentication Code：应用程序鉴别符，3个连续的ASCII字符("2.0")。|
|n|Data Sub-Blocks：应用程序数据块，由一个或多个数据块组成。|
|最后一个字节|Block Terminator：应用程序扩展块结束标识，固定值为 0。|

#### Comment Extension

<img src="\图片\comment_ext.gif" alt="comment_ext" style="zoom:150%;" />

|字节|描述|
|-|-|
|1|Extension Introducer：扩展块标识，固定值为 21。|
|2|Comment Label：注释扩展块标识，固定值为 FE。|
|n|Comment Data：注释数据块，由一个或多个数据库组成。|
|最后一个字|Block Terminator：注释扩展块结束标识，固定值为 0。|

#### Trailer

<img src="\图片\trailer_block.gif" alt="trailer_block" style="zoom:150%;" />
尾部标记，表示GIF的字节内容到此结束，固定值为 3B(分号)。

