package com.ccb.cloud.simis.accountability.approval.service

import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil
import java.io.*
import java.text.SimpleDateFormat

/*
 * 生成通用service
 */
packageName = ""
typeMapping = [
        (~/(?i)tinyint|smallint|mediumint/)      : "Integer",
        (~/(?i)int/)                             : "Long",
        (~/(?i)bool|bit/)                        : "Boolean",
        (~/(?i)float|double|decimal|real/)       : "Double",
        (~/(?i)datetime|timestamp|date|time/)    : "Date",
        (~/(?i)blob|binary|bfile|clob|raw|image/): "InputStream",
        (~/(?i)/)                                : "String"
]


FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaClassName(table.getName(), true)
    def clazzName = javaClassName(table.getName(), false)
    def classNamePO = javaClassName(table.getName(), true) + "PO"
    def clazzNamePO = javaClassName(table.getName(), false) + "PO"
    def classNameDao = javaClassName(table.getName(), true) + "Dao"
    def clazzNameDao = javaClassName(table.getName(), false) + "Dao"
    def classNameInVo = javaClassName(table.getName(), true) + "InVo"
    def clazzNameInVo = javaClassName(table.getName(), false) + "InVo"
    def classNameService = javaClassName(table.getName(), true) + "Service"
    def clazzNameService = javaClassName(table.getName(), false) + "Service"
    def classNameServiceImpl = javaClassName(table.getName(), true) + "ServiceImpl"
    def fields = calcFields(table)
    packageName = getPackageName(dir)
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, className + "ServiceImpl.java")), "UTF-8"))
    printWriter.withPrintWriter {out
        -> generate(out,
                className, clazzName, classNamePO, clazzNamePO,
                classNameDao, clazzNameDao,classNameInVo,clazzNameInVo,
                classNameService, clazzNameService,classNameServiceImpl,
                fields,table)}
}

// 获取包所在文件夹路径
def getPackageName(dir) {
    return dir.toString().replaceAll("\\\\", ".").replaceAll("/", ".").replaceAll("^.*src(\\.main\\.java\\.)?", "") + ";"
}

def generate(out,
             className, clazzName,classNamePO,clazzNamePO,
             classNameDao, clazzNameDao,classNameInVo,clazzNameInVo,
             classNameService, clazzNameService,classNameServiceImpl,
             fields,table) {
    out.println "package $packageName"
    out.println ""
    out.println "import com.ccb.cloud.pub.common.CommonOutVo;"
    out.println "import com.ccb.cloud.pub.common.CommonUtils;"
    out.println "import com.ccb.openframework.datatransform.message.TxRequestMsg;"
    out.println "import org.springframework.beans.factory.annotation.Autowired;"
    out.println "import org.springframework.stereotype.Service;"
    out.println "import org.apache.commons.lang.StringUtils;"
    out.println ""
    out.println "import javax.transaction.Transactional;"
    out.println "import java.util.UUID;"
    Set types = new HashSet()

    fields.each() {
        types.add(it.type)
    }

    if (types.contains("Date")) {
        out.println "import java.util.Date;"
    }

    if (types.contains("InputStream")) {
        out.println "import java.io.InputStream;"
    }
    out.println ""
    out.println "/**\n" +
            " * @Description " + table.getComment()+"Service Implements\n" +
            " * @Author  yuwei\n" +
            " * @Date "+ new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " \n" +
            " */"
    out.println "@Service"
    out.println "@Transactional"
    out.println "public class $classNameServiceImpl implements $classNameService {"
    out.println ""

    out.println("\t@Autowired")
    out.println("\tprivate $classNameDao $clazzNameDao;\n")

    out.println("\t@Override")
    out.println "\tpublic CommonOutVo findOne(TxRequestMsg msg){\n" +
            "\t\t$classNameInVo inVo = CommonUtils.getInVo(msg, $classNameInVo" + ".class);\n" +
            "\t\tString pkId = inVo.getPkId();\n" +
            "\t\t$classNamePO po = $clazzNameDao" + ".findById(pkId).orElse(null);\n" +
            "\t\treturn CommonOutVo.ok(po);\n" +
            "\t}\n"

    out.println("")
    out.println("\t@Override")
    out.println "\tpublic CommonOutVo save(TxRequestMsg msg){\n" +
            "\t$classNamePO po = CommonUtils.getObject(msg, $classNameInVo" + ".class, $classNamePO" + ".class);\n" +

            "\t\tif(StringUtils.isEmpty(po.getPkId())){\n" +
            "\t\t\tpo.setPkId(UUID.randomUUID().toString());\n" +
            "\t\t}\n\n" +
            "\t\t" + clazzNameDao + ".save(po);\n" +
            "\t\treturn CommonOutVo.ok(po);\n" +
            "\t}\n";

    out.println("\t@Override")
    out.println "\tpublic CommonOutVo delete(TxRequestMsg msg){\n" +
            "\t\t$classNameInVo inVo = CommonUtils.getInVo(msg, $classNameInVo" + ".class);\n" +
            "\t\tString pkId = inVo.getPkId();\n" +
            "\t\t$clazzNameDao" + ".deleteById(pkId);\n" +
            "\t\treturn CommonOutVo.ok();\n" +
            "\t}\n"

    out.println("\t@Override")
    out.println "\tpublic CommonOutVo list(TxRequestMsg msg){\n" +

            "\t\t$classNameInVo inVo = CommonUtils.getInVo(msg, $classNameInVo" + ".class);\n" +
            "\t\tString relationId = inVo.getRelationId();\n" +
            "\t\tList<" + classNamePO + "> list = $clazzNameDao" + ".findByRelationId(relationId);\n" +
            "\t\treturn CommonOutVo.ok(list);\n" +
            "\t}\n"

    out.println("\t@Override")
    out.println "\tpublic void deleteByRelationId(String relationId){\n" +
            "\t\t$clazzNameDao" + ".deleteByRelationId(relationId);\n" +
            "\t}\n"

    out.println "}"
}
def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())

        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        def comm =[
                colName : col.getName(),
                name :  javaName(col.getName(), false),
                type : typeStr,
                commoent: col.getComment(),
                annos: "\t@Column(name = \""+col.getName()+"\" )"]
        if("pk_id".equals(Case.LOWER.apply(col.getName())))
            comm.annos =  " @Id\n" + comm.annos;
        fields += [comm]
    }
}

// 处理类名（这里是因为我的表都是以t_命名的，所以需要处理去掉生成类名时的开头的T，
// 如果你不需要那么请查找用到了 javaClassName这个方法的地方修改为 javaName 即可）
def javaClassName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    // 去除开头的T  http://developer.51cto.com/art/200906/129168.htm
    s = s[1..s.size() - 1]
    capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def javaName(str, capitalize) {
//    def s = str.split(/(?<=[^\p{IsLetter}])/).collect { Case.LOWER.apply(it).capitalize() }
//            .join("").replaceAll(/[^\p{javaJavaIdentifierPart}]/, "_")
//    capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def isNotEmpty(content) {
    return content != null && content.toString().trim().length() > 0
}

static String changeStyle(String str, boolean toCamel){
    if(!str || str.size() <= 1)
        return str

    if(toCamel){
        String r = str.toLowerCase().split('_').collect{cc -> Case.LOWER.apply(cc).capitalize()}.join('')
        return r[0].toLowerCase() + r[1..-1]
    }else{
        str = str[0].toLowerCase() + str[1..-1]
        return str.collect{cc -> ((char)cc).isUpperCase() ? '_' + cc.toLowerCase() : cc}.join('')
    }
}

static String genSerialID()
{
    return "\tprivate static final long serialVersionUID =  "+Math.abs(new Random().nextLong())+"L;"
}
