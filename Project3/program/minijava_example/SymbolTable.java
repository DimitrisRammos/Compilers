import java.lang.reflect.Method;
import java.util.*;

class PairElements {
    public String typename;
    public String name;

    PairElements( String T, String N){
        typename = T;
        name = N;
    }

    public String getKey(){
        return  typename;
    }

    public  String getValue(){
        return  name;
    }
    public void changeKey( String key) {
        typename = key;
    }

}

class Methods{

    public String Name_methods;
    public String typename;

    public String classname;

    public LinkedList< PairElements> ArgumentList;
    public LinkedList< PairElements> Variables;

    Methods( String Name_methods, String typename, String classname){

        this.Name_methods = Name_methods;
        this.typename = typename;
        this.classname = classname;

        ArgumentList = new LinkedList<>();
        Variables = new LinkedList<>();
    }

    public void addArgument( String type, String name) throws Exception{
        for(PairElements pair_e: ArgumentList){
            String name_ = pair_e.getValue();
            if( name_.equals(name)){
                throw  new Exception("\nWrong name for variable with name: " + name + " -- in class with name: " +"\n");
            }
        }
        PairElements pair_e = new PairElements( type, name);
        ArgumentList.push( pair_e);

    }

    public void addVariable( String type, String name) throws Exception{
        for( PairElements pair_e: Variables){
            String name_ = pair_e.getValue();
            if( name_.equals(name)){
                throw  new Exception("\nWrong name for variable with name: " + name + " -- in class with name: " +"\n");
            }

        }

        for(PairElements pair_e1: ArgumentList){
            String name_1 = pair_e1.getValue();
            if( name_1.equals(name)){
                throw  new Exception("\nWrong name for variable with name: " + name + " -- is equal with argument variable: " +"\n");
            }
        }

        PairElements pair_e = new PairElements( type, name);
        Variables.push( pair_e);
    }

    public LinkedList<PairElements> takeArgumentList(){
        return ArgumentList;
    }

    public LinkedList<PairElements> takeVariables(){
        return Variables;
    }

    public String getReturnType(){ return typename; }

    public void CheckListArguments( String[] words, LinkedHashMap<String, String> hmap) throws Exception{
        if(words == null){
            return;
        }



        int i = words.length - 1;

        for(PairElements pair_e:ArgumentList)
        {
            String name = pair_e.getValue();
            String typename = pair_e.getKey();
            String w = words[i];
             w = w.replaceAll("\\s", "");



            if( !typename.equals(w))
            {

                if( !typename.equals("int") && !typename.equals("boolean") && !typename.equals("boolean[]") && !typename.equals("int[]"))
                {
                    if( hmap.containsKey(w))
                    {
                        String ext_c = hmap.get(w);
                        if( ext_c.equals(typename))
                        {
                            i--;
                            continue;
                        }
                    }
                }

            }
            i--;
        }

    }
}



public class SymbolTable {
    LinkedHashMap<String, String> hash_map_class;

    LinkedHashMap<String, LinkedList<PairElements>> Variables;

    LinkedHashMap<String, LinkedHashMap<String, Methods>> Methods_for_class;

    LinkedHashMap<String, LinkedList<PairElements>> Vtable;
    String nameMethodNow;
    String MainClass;
    String nameClassNow;
    public int num;
    public int Register;
    public LinkedHashMap<String,String> map_reg_name;

    public LinkedHashMap<String, String> map_registers;

    public SymbolTable() {

        hash_map_class = new LinkedHashMap<>();
        Variables = new LinkedHashMap<>();
        Methods_for_class = new LinkedHashMap<>();
        Vtable = new LinkedHashMap<>();

        nameMethodNow = null;
        nameClassNow = null;
        MainClass = null;
        Register = 0;
        num = 0;
        map_registers = new LinkedHashMap<>();
        map_reg_name = new LinkedHashMap<>();
    }

    public void AddClass(String class_, String class_extend) throws Exception {
        if (!hash_map_class.containsKey(class_)) {
            if (!hash_map_class.containsKey(class_extend)) {
                if (class_extend.equals("null")) {
                    if (hash_map_class.size() == 0) {
                        MainClass = class_;
                    }
                    hash_map_class.put(class_, class_extend);

                } else {
                    throw new Exception("\nWrong classs from extented\n");
                }
                //error
            } else {
                hash_map_class.put(class_, class_extend);

            }
        } else {
            throw new Exception("\nWrong name classs\n");
        }
    }

    public void AddVar(String type, String identifier, String classname) throws Exception {
        if (!Variables.containsKey(classname)) {

            LinkedList<PairElements> list = new LinkedList<>();
            PairElements pair_e = new PairElements(type, identifier);
            list.push(pair_e);
            Variables.put(classname, list);
        } else {
            LinkedList<PairElements> list = Variables.get(classname);

            for (PairElements pair_e : list) {
                String name = pair_e.getValue();
                if (name.equals(identifier)) {
                    throw new Exception("\nWrong name for variable with name: " + identifier + " -- in class with name: " + classname + "\n");
                }
            }
            PairElements pair_e = new PairElements(type, identifier);
            list.push(pair_e);

        }
    }

    public void AddMethod(String classname, String type, String nameMethod) throws Exception {
        if (!Methods_for_class.containsKey(classname)) {
            LinkedHashMap<String, Methods> map_methods = new LinkedHashMap<>();
            Methods meth = new Methods(nameMethod, type, classname);
            map_methods.put(nameMethod, meth);
            Methods_for_class.put(classname, map_methods);
            nameMethodNow = nameMethod;
            nameClassNow = classname;
        } else {
            LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(classname);
            if (!map_methods.containsKey(nameMethod)) {
                Methods meth = new Methods(nameMethod, type, classname);
                map_methods.put(nameMethod, meth);
                nameMethodNow = nameMethod;
                nameClassNow = classname;
            } else {
                throw new Exception("\nWrong name -- this Methods is defined: " + nameMethod + "\n");
            }
        }


    }

    public void CheckTheOverrideClass() throws Exception {
        String ext_class = hash_map_class.get(nameClassNow);
        if (!ext_class.equals("null")) {
            LinkedHashMap<String, Methods> map_methods_ext = Methods_for_class.get(ext_class);
            if (map_methods_ext.containsKey(nameMethodNow)) {

                LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
                Methods meth = map_methods.get(nameMethodNow);
                Methods meth_ext = map_methods_ext.get(nameMethodNow);

                //check the type
                String typename_meth = meth.getReturnType();
                String typename_meth_ext = meth_ext.getReturnType();

                //check return type
                if (!typename_meth_ext.equals(typename_meth)) {
                    throw new Exception("\nError type in override method in class " + nameClassNow + " for method " + nameMethodNow + "\n");
                }

                //check arguments
                List<PairElements> list_ext = meth_ext.takeArgumentList();
                List<PairElements> list_meth = meth.takeArgumentList();

                if (list_ext.size() != list_meth.size()) {
                    throw new Exception("\nError length arguments in override method in class " + nameClassNow + " for method " + nameMethodNow + "\n");
                }

                int i = 0;
                for (PairElements pair_e_ext : list_ext) {
                    PairElements pair_e_meth = list_meth.get(i);

                    String typename_ext = pair_e_ext.getKey();
                    String typename_meth_ = pair_e_meth.getKey();
                    if (!typename_ext.equals(typename_meth_)) {
                        throw new Exception("\nError with type in override method in class " + nameClassNow + " for method " + nameMethodNow + "\n");
                    }
                    i++;

                }

            }
        }
    }

    public void AddArgumentsMethod(String classname, String type, String name) throws Exception {
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(classname);

        Methods meth = map_methods.get(nameMethodNow);
        meth.addArgument(type, name);
    }

    public void AddVariables_forMethods(String type, String identifier) throws Exception {
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
        Methods meth = map_methods.get(nameMethodNow);
        meth.addVariable(type, identifier);
    }


    public void checkAll() throws Exception {

        for (String key : Variables.keySet()) {

            LinkedList<PairElements> list = Variables.get(key);

            for (PairElements pair_e : list) {
                String type = pair_e.getKey();
                if (type.equals("int")) {
                    continue;
                }
                if (type.equals("int[]")) {
                    continue;
                }
                if (type.equals("boolean")) {
                    continue;
                }
                if (type.equals("boolean[]")) {
                    continue;
                }

                if (!hash_map_class.containsKey(type)) {
                    throw new Exception("\nWrong type. This type is not defined. The type is: " + type + "\n");
                }

            }
        }

        for (String key : Methods_for_class.keySet()) {
            LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(key);
            for (String key_1 : map_methods.keySet()) {
                Methods meth = map_methods.get(key_1);

                LinkedList<PairElements> list_arguments = meth.takeArgumentList();
                LinkedList<PairElements> list_variables = meth.takeVariables();

                for (PairElements pair_e1 : list_arguments) {
                    String type = pair_e1.getKey();
                    if (type.equals("int")) {
                        continue;
                    }
                    if (type.equals("int[]")) {
                        continue;
                    }
                    if (type.equals("boolean")) {
                        continue;
                    }
                    if (type.equals("boolean[]")) {
                        continue;
                    }

                    if (!hash_map_class.containsKey(type)) {
                        throw new Exception("\nWrong type. This type is not defined. The type is: " + type + "\n");
                    }
                }

                for (PairElements pair_e2 : list_variables) {
                    String type = pair_e2.getKey();
                    if (type.equals("int")) {
                        continue;
                    }
                    if (type.equals("int[]")) {
                        continue;
                    }
                    if (type.equals("boolean")) {
                        continue;
                    }
                    if (type.equals("boolean[]")) {
                        continue;
                    }

                    if (!hash_map_class.containsKey(type)) {
                        throw new Exception("\nWrong type. This type is not defined. The type is: " + type + "\n");
                    }
                }
            }
        }

    }

    public void NowClass(String classname) {
        nameClassNow = classname;
    }

    public void NowMethod(String method) {
        map_registers.clear();
        nameMethodNow = method;
        num = 0;
    }

    public String getClassNow() {
        return nameClassNow;
    }

    public void AddNum(int inputnum){
        num = num + inputnum;
    }

    public int getNum(){
        return num;
    }
    public String getType(String identifier) throws Exception {


        if (nameMethodNow.equals("Main")) {
            LinkedList<PairElements> list = Variables.get(nameClassNow);

            for (PairElements pair_e : list) {
                String type = pair_e.getKey();
                String ident = pair_e.getValue();

                if (ident.equals(identifier)) {
                    return type;
                }
            }
        } else {
            LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
            Methods meth = map_methods.get(nameMethodNow);
            LinkedList<PairElements> list_1 = meth.takeArgumentList();
            if (list_1 != null) {

                for (PairElements pair_e : list_1) {
                    String type = pair_e.getKey();
                    String ident = pair_e.getValue();

                    if (ident.equals(identifier)) {
                        return type;
                    }
                }
            }

            LinkedList<PairElements> list_2 = meth.takeVariables();
            if (list_2 != null) {
                for (PairElements pair_e : list_2) {
                    String type = pair_e.getKey();
                    String ident = pair_e.getValue();

                    if (ident.equals(identifier)) {
                        return type;
                    }
                }
            }


            LinkedList<PairElements> list_3 = Variables.get(nameClassNow);
            if (list_3 != null) {
                for (PairElements pair_e : list_3) {
                    String type = pair_e.getKey();
                    String ident = pair_e.getValue();

                    if (ident.equals(identifier)) {
                        return type;
                    }
                }
            }


            boolean find = false;
            String cl_now = nameClassNow;
            String cl_ext = hash_map_class.get(cl_now);
            while (!find)
            {
                if( cl_ext.equals("null"))
                {
                    find = true;
                    break;
                }

                cl_now = cl_ext;


                LinkedList<PairElements> list_ext = Variables.get(cl_now);
                if (list_ext != null) {
                    for (PairElements pair_e : list_ext) {
                        String type = pair_e.getKey();
                        String ident = pair_e.getValue();

                        if (ident.equals(identifier)) {
                            find = true;
                            return type;
                        }
                    }
                }

                cl_ext = hash_map_class.get(cl_now);
            }



        }
        return null;
    }

    public String CheckIt(String type) throws Exception {
        if (type.equals("boolean[]")) {
            return "boolean";
        }
        if (type.equals("int[]")) {
            return "int";
        }

        if (type.equals("int")) {
            throw new Exception("\nError in ArrayLookUp\n");
        }
        if (type.equals("boolean")) {
            throw new Exception("\nError in ArrayLookUp\n");
        }

        if (type.equals("this")) {
            throw new Exception("\nError in ArrayLookUp\n");
        }

        String result = getType(type);
        if (result.equals("int[]")) {
            return "int";
        }
        if (result.equals("boolean[]")) {
            return "boolean";
        }

        throw new Exception("Error type in ArrayLookUp");

    }

    public void CheckIfIsClass(String classname) throws Exception {
        if (!hash_map_class.containsKey(classname)) {
            throw new Exception("\nThis Class Isn't Defined\n");
        }
    }


    public String[] CheckTheCallClass(String name, String meth, String expr) throws Exception {
        if(name.equals("%this"))
        {
            name = nameClassNow;
        }
        else if( name.startsWith("%"))
        {
            name = map_reg_name.get(name);
        }
        if (name.equals("this")) {
            LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);


            return CheckArguments(nameClassNow, meth, expr);

        } else if (hash_map_class.containsKey(name)) {

            String classname = checkTheMethods(name, meth);
            return CheckArguments(classname, meth, expr);
        } else {
            String type = null;
            LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);

            if (nameMethodNow.equals("Main")) {

                LinkedList<PairElements> list = Variables.get(nameClassNow);
                if (list != null) {
                    for (PairElements pair_e : list) {
                        String typename = pair_e.getKey();
                        String name_v = pair_e.getValue();
                        if (name.equals(name_v)) {
                            type = typename;
                            break;
                        }
                    }
                }
            } else {


                Methods meth_1 = map_methods.get(nameMethodNow);

                //check argyments methods
                List<PairElements> list1 = meth_1.takeArgumentList();
                if (list1 != null) {
                    for (PairElements pair_e : list1) {
                        String typename = pair_e.getKey();
                        String name_v = pair_e.getValue();
                        if (name.equals(name_v)) {
                            type = typename;
                            break;
                        }
                    }
                }


                //check variables methods
                List<PairElements> list2 = meth_1.takeVariables();
                if (list2 != null) {
                    for (PairElements pair_e : list2) {
                        String typename = pair_e.getKey();
                        String name_v = pair_e.getValue();
                        if (name.equals(name_v)) {
                            type = typename;
                            break;
                        }
                    }
                }

                //check variables class
                if (type == null) {
                    List<PairElements> list3 = Variables.get(nameClassNow);
                    for (PairElements pair_e : list3) {
                        String typename = pair_e.getKey();
                        String name_v = pair_e.getValue();
                        if (name.equals(name_v)) {
                            type = typename;
                            break;
                        }
                    }
                }
            }



            String classname = checkTheMethods(type, meth);

            return CheckArguments(classname, meth, expr);

        }

    }

    public String checkTheMethods(String type, String method) throws Exception {
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(type);

        String classname = type;


        String ext_class = hash_map_class.get(type);
        if (!ext_class.equals("null")) {
            map_methods = Methods_for_class.get(ext_class);
            if (map_methods.containsKey(method)) {
                classname = ext_class;
            }

        }


        return classname;
    }

    public String[] CheckArguments(String classname, String methodName, String expr) throws Exception {



        if (expr == null) {
            String result;
            LinkedHashMap<String, Methods> map_methods1 = Methods_for_class.get(classname);

            Methods meth_1 = map_methods1.get(methodName);

            result = meth_1.getReturnType();

            String[] w2 = new String[1];
            w2[0] = result;
            return w2;
        }
        String[] words = expr.split(",");
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);



        int i = 0;
        for (String w1 : words) {

            String w = w1.replaceAll("\\s", "");
            String type = null;


            if ((!w.equals("int")) && (!w.equals("boolean")) && (!w.equals("int[]")) && (!w.equals("boolean[]"))) {
                if (w.equals("this")) {
                    words[i] = nameClassNow;

                } else {

                    //check the variables from method & argument method

                    if (!nameMethodNow.equals("Main")) {
                        Methods meth_2 = map_methods.get(nameMethodNow);
                        List<PairElements> list1 = meth_2.takeArgumentList();

                        if(list1 !=null) {
                            for (PairElements pair_e : list1) {
                                String t = pair_e.getValue();
                                if (t.equals(w)) {

                                    type = pair_e.getKey();
                                    break;
                                }

                            }
                            if (type != null) {
                                words[i] = type;
                                i++;
                                continue;
                            }
                        }


                        List<PairElements> list2 = meth_2.takeVariables();
                        if(list2!=null) {
                            for (PairElements pair_e : list2) {
                                String t = pair_e.getValue();
                                if (t.equals(w)) {
                                    type = pair_e.getKey();
                                    break;
                                }
                            }

                            if (type != null) {
                                words[i] = type;
                                i++;
                                continue;
                            }
                        }
                    }

                    //check the variables from class
                    List<PairElements> list3 = Variables.get(nameClassNow);
                    if(list3!=null) {
                        for (PairElements pair_e : list3) {
                            String t = pair_e.getValue();
                            if (t.equals(w)) {
                                type = pair_e.getKey();
                                words[i] = type;
                                break;
                            }
                        }
                    }


                }
            }
            i++;
        }


        LinkedHashMap<String, Methods> map_methods1 = Methods_for_class.get(classname);

        Methods meth_1 = map_methods1.get(methodName);


        String result;
        result = meth_1.getReturnType();

        meth_1.CheckListArguments(words, hash_map_class);


        String[] w2 = new String[words.length + 1];
        w2[0] = result;
        int i1 = 1;
        for(String w: words)
        {
            w2[i1] = w;
        }

        return w2;
    }


    public void PrintTheVariables() {
        int num = 0;
        boolean flag = true;
        for (String str : Variables.keySet()) {

            if (str.equals(MainClass)) {
                continue;
            }
            LinkedList<PairElements> list = Variables.get(str);


            for (PairElements pair_e : list) {
                String typename = pair_e.getKey();
                String name = pair_e.getValue();

                if (flag) {
                    flag = false;
                    System.out.println(str + "." + name + " : " + num);
                } else {
                    if (typename.equals("int")) {
                        num = num + 4;
                    } else if (typename.equals("boolean")) {
                        num = num + 1;
                    } else {
                        num = num + 8;
                    }
                    System.out.println(str + "." + name + " : " + num);

                }


            }
        }


    }


    public void checkTypeReturn(String type) throws Exception {
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
        Methods meth = map_methods.get(nameMethodNow);
        String type_return = meth.getReturnType();
        if (!type_return.equals(type)) {

            throw new Exception("\nError return type in method: " + nameMethodNow + "   " + type_return + type + "\n");
        }
    }

    public String getreturnType(){
        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
        Methods meth = map_methods.get(nameMethodNow);
        return meth.getReturnType();
    }

    public void CreateVtable() {

        //main vale
        Vtable.put( MainClass, null);

        //class
        for (String key : Methods_for_class.keySet()) {

            LinkedList< PairElements> list = new LinkedList<>();

            LinkedHashMap<String, Methods> map = Methods_for_class.get(key);

            String ext_class = hash_map_class.get(key);
            if(!ext_class.equals("null"))
            {
                LinkedList<PairElements> list1 = Vtable.get(ext_class);
                for(PairElements pair_e: list1){
                    String cl = pair_e.getKey();
                    String meth = pair_e.getValue();
                    PairElements pair_e1 = new PairElements(cl, meth);
                    list.push( pair_e1);
                }
            }

            for (String key_1 : map.keySet()) {

                if (!ext_class.equals("null")) {
                    boolean find = false;

                        LinkedList<PairElements> list1 = Vtable.get(ext_class);

                        String cl = key;
                        String meth = key_1;
                        for ( PairElements pair_e: list1)
                        {
                               cl = pair_e.getKey();
                               meth = pair_e.getValue();
                               if( meth.equals(key_1))
                               {
                                   for(PairElements pair_e1: list)
                                   {
                                       String meth_e1 = pair_e1.getValue();
                                       if( meth_e1.equals(meth)) {
                                           pair_e1.changeKey( key);
                                       }
                                   }

                                   find = true;
                                   break;
                               }
                        }

                        if( !find)
                        {

                            PairElements pair_e1 = new PairElements(key, key_1);
                            list.push( pair_e1);
                        }
                }
                else {
                    PairElements pair_e1 = new PairElements(key,key_1);
                    list.push( pair_e1);
                }
            }

            Vtable.put( key, list);
        }

    }

    public void printVtable(){
        for ( String Key: Vtable.keySet())
        {

            LinkedList<PairElements> list = Vtable.get(Key);
            System.out.println("For class - " + Key);
            if(list == null)
            {
                System.out.println("null");
                continue;
            }
            System.out.println();
            for( PairElements pair_e: list){
                String cl = pair_e.getKey();
                String meth = pair_e.getValue();
                System.out.println("    class == " + cl + " method == " + meth);
            }
        }
    }

    public void printLLVtable(){
        for ( String Key: Vtable.keySet())
        {

            LinkedList<PairElements> list = Vtable.get(Key);
            if(list == null)
            {
                System.out.println("@." + Key + "_vtable = global[0 x i8*][]\n");
                continue;
            }
            int size = list.size();
            System.out.println("@." + Key + "_vtable = global[" + size + " x i8*][");
            int i = 0;
            for( PairElements pair_e: list){

                String cl = pair_e.getKey();
                String meth = pair_e.getValue();

                LinkedHashMap< String, Methods> meth_map = Methods_for_class.get(cl);
                Methods meth_c = meth_map.get(meth);

                String type_return = meth_c.getReturnType();
                LinkedList<PairElements> list_meth = meth_c.takeArgumentList();

                LinkedList<String> list_type_arg = new LinkedList<>();

                for( PairElements pair_e1: list_meth)
                {

                    String key = pair_e1.getKey();
                    list_type_arg.push(key);
                }
                if(type_return.equals("int"))
                {
                    type_return = "i32";
                } else if (type_return.equals("boolean")) {
                    type_return = "i1";
                }
                else {
                    type_return = "i8*";
                }

                System.out.print("    i8* bitcast (" + type_return + " (i8*");
                for( String str1: list_type_arg){

                    if(str1.equals("int"))
                    {
                        str1 = "i32";
                    } else if (str1.equals("boolean")) {
                        str1 = "i1";
                    }
                    else {
                        str1 = "i8*";
                    }
                    System.out.print("," + str1);

                }
                i++;
                if( size == i)
                {
                    System.out.println(")* @" + cl + "." + meth + " to i8*)");
                }
                else {
                    System.out.println(")* @" + cl + "." + meth + " to i8*),");
                }


            }
            System.out.println("]\n");
        }

        System.out.println("declare i8* @calloc(i32, i32)");
        System.out.println("declare i32 @printf(i8*, ...)");
        System.out.println("declare void @exit(i32)\n");
        System.out.println("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
        System.out.println("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"");
        System.out.println("@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"");
        System.out.println("define void @print_int(i32 %i) {");
        System.out.println("    %_str = bitcast [4 x i8]* @_cint to i8*");
        System.out.println("    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
        System.out.println("    ret void");
        System.out.println("}\n");
        System.out.println("define void @throw_oob() {");
        System.out.println("    %_str = bitcast [15 x i8]* @_cOOB to i8*");
        System.out.println("    call i32 (i8*, ...) @printf(i8* %_str)");
        System.out.println("    call void @exit(i32 1)");
        System.out.println("    ret void");
        System.out.println("}\n");
        System.out.println("define void @throw_nsz() {");
        System.out.println("    call i32 (i8*, ...) @printf(i8* %_str)");
        System.out.println("    call void @exit(i32 1)");
        System.out.println("    ret void");
        System.out.println("}\n");



    }

    public void printLLmethod(){
        LinkedHashMap< String, Methods> meth_map = Methods_for_class.get(nameClassNow);
        Methods meth_c = meth_map.get(nameMethodNow);

        String type_return = meth_c.getReturnType();
        LinkedList<PairElements> list_meth = meth_c.takeArgumentList();

        LinkedList<PairElements> list_type_arg = new LinkedList<>();

        for( PairElements pair_e1: list_meth)
        {

            String key = pair_e1.getKey();
            if(key.equals("int"))
            {
                key = "i32";
            }
            else if(key.equals("boolean"))
            {
                key = "i1";
            }
            else
            {
                key = "i8*";
            }

            String value = pair_e1.getValue();

            PairElements pair = new PairElements(key, value);
            list_type_arg.push(pair);
        }
        if(type_return.equals("int"))
        {
            type_return = "i32";
        } else if (type_return.equals("boolean")) {
            type_return = "i1";
        }
        else {
            type_return = "i8*";
        }

        System.out.print("define " + type_return + " @" + nameClassNow + "." + nameMethodNow + "(i8* %this");
        for( PairElements pair: list_type_arg){

            String key = pair.getKey();
            String value = "%." + pair.getValue();
            System.out.print("," + key + " " + value);
        }
        System.out.println(") {");

        for( PairElements pair: list_type_arg){
            String type = pair.getKey();
            String identifier = pair.getValue();
            System.out.println("    %" + identifier + " = alloca " + type);
            System.out.println("    store " + type + " %." + identifier + ", " + type + "* %" + identifier  + "\n");
        }
    }

    public String CheckforIdentifier(String identifier){

        if( nameMethodNow.equals("Main"))
        {
            LinkedList<PairElements> list = Variables.get(MainClass);
            if(list!=null)
            {
                for (PairElements pair : list) {
                    String name = pair.getValue();
                    if (name.equals(identifier)) {
                        return null;
                    }
                }
            }
            return null;
        }

        LinkedHashMap<String, Methods> map_methods = Methods_for_class.get(nameClassNow);
        Methods meth = map_methods.get(nameMethodNow);
        LinkedList<PairElements> list_argu = meth.takeArgumentList();
        LinkedList<PairElements> list_variabl = meth.takeVariables();

        if( list_argu != null) {
            for (PairElements pair : list_argu) {
                String name = pair.getValue();
                if (name.equals(identifier)) {
                    return null;
                }
            }
        }

        if( list_variabl != null) {
            for (PairElements pair : list_variabl) {
                String name = pair.getValue();
                if (name.equals(identifier)) {
                    return null;
                }
            }
        }

        LinkedList<PairElements> list_variables_class = Variables.get(nameClassNow);
        String type = "";
        String cl_now = nameClassNow;
        while(true) {
            if (list_variables_class != null) {
                for (PairElements pair : list_variables_class) {
                    String name = pair.getValue();
                    if (name.equals(identifier)) {
                        type = pair.getKey();
                        if (type.equals("int")) {
                            type = "i32";
                        } else if (type.equals("boolean")) {
                            type = "i1";
                        } else {
                            type = "i8*";
                        }
                    }
                }
            }

            if (type.equals("")) {
                String cl = hash_map_class.get(cl_now);
                if( cl.equals("null"))
                {
                    return null;
                }
                cl_now = cl;
                list_variables_class = Variables.get(cl_now);
            }
            else {
                break;
            }
        }
        int Reg = Register;

        System.out.println("    %_" + Reg + " = getelementptr i8, i8* %this, " + type + " 8");
        System.out.println("    %_" + (Reg + 1) +" = bitcast i8* %_1 to " + type+ "*");
        Register += 2;
        String result = String.valueOf(Reg + 1);
        return "%_" + result;
    }
}