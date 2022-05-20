import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.reflect.Array;

public class FirstVisitor  extends GJDepthFirst<String, String> {

    public SymbolTable STable;

    public FirstVisitor(){
        STable = new SymbolTable();
    }

    public SymbolTable takeStable(){ return STable;}
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */

    @Override
     public String visit(MainClass n, String argu) throws Exception {
         String classname = n.f1.accept(this, null);
         System.out.println("Class: " + classname);

         STable.AddClass( classname, "null");


         n.f14.accept(this, classname );


         return null;
     }


     @Override
     public String visit( VarDeclaration n, String argu) throws Exception{

        if( argu.equals("1")){
            String type = n.f0.accept(this, argu);
            String identifier = n.f1.accept(this, argu);
            STable.AddVariables_forMethods( type, identifier);
        }
        else {

            String type = n.f0.accept(this, argu);
            String identifier = n.f1.accept(this, argu);
            STable.AddVar(type, identifier, argu);
        }

        return  null;
     }

    @Override
    public String visit( Identifier n, String argu) throws Exception{
        return n.f0.toString();
    }

    @Override
    public String visit( BooleanArrayType n, String argu) throws Exception{
        return "boolean[]";
    }

    @Override
    public String visit( IntegerArrayType n, String argu) throws Exception{
        return "int[]";
    }

    @Override
    public String visit( BooleanType n, String argu) throws Exception{
        return "boolean";
    }

    @Override
    public String visit( IntegerType n, String argu) throws Exception{
        return "int";
    }



     /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */

    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        STable.AddClass( classname, "null");

        n.f3.accept( this, classname);
        n.f4.accept( this, classname);
        System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */

    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);
        String extend_class = n.f3.accept(this, null);
        STable.AddClass( classname, extend_class);

        n.f5.accept( this, classname);
        n.f6.accept( this, classname);
        System.out.println();

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */

    public String visit(MethodDeclaration n, String argu) throws Exception {
        String myType = n.f1.accept(this, argu);
        String myName = n.f2.accept(this, argu);

        STable.AddMethod( argu, myType, myName);

        String argumentList = n.f4.present() ? n.f4.accept(this, argu) : "";

        STable.CheckTheOverrideClass();
        n.f7.accept( this, "1");

        System.out.println(myType + " " + myName + " -- " + argumentList);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */

    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null) {
            ret += n.f1.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */

    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */

    public String visit(FormalParameter n, String argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        STable.AddArgumentsMethod( argu, type, name);

        return type + " " + name;
    }

}
