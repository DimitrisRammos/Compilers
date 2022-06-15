import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.LinkedHashMap;

public class SecondVisitor  extends GJDepthFirst<String, String> {

    public SymbolTable STable;

    public SecondVisitor( SymbolTable s){
        STable = s;
    }

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
        STable.checkAll();
        STable.PrintTheVariables();

        String classname = n.f1.accept(this, null);
        STable.NowClass( classname);
        STable.NowMethod("Main");

        n.f15.accept( this, classname);

        return null;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */

    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {

        String classname = n.f1.accept(this, argu);
        STable.NowClass(classname);
        n.f4.accept(this, classname);
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

    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {

        String classname = n.f1.accept(this, argu);
        STable.NowClass(classname);
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

    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String meth = n.f2.accept( this, argu);
        STable.NowMethod(meth);
        n.f8.accept(this, argu);

        String expr_1 =n.f10.accept(this, argu);
        String type = expr_1;

        if( !expr_1.equals("int") &&  !expr_1.equals("int[]") &&  !expr_1.equals("boolean") && !expr_1.equals("boolean[]") )
        {
            if(expr_1.equals("this"))
            {
                type = STable.getClassNow();
            }
            else
            {
                type = STable.getType( expr_1);
            }

        }

        STable.checkTypeReturn(type);


        return null;
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */




    @Override
    public String visit( Statement n, String argu) throws Exception{
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */

    @Override
    public String visit(Block n, String argu) throws Exception {
        n.f1.accept(this, argu);

        return null;
    }

//////////////////////////////////

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */

    @Override
    public String visit(Expression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */

    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {

        String identifier = n.f0.accept(this, argu);

        String type = STable.getType( identifier);

        String the_type = n.f2.accept(this, argu);

        if( !(STable.hash_map_class.containsKey(the_type)) && !the_type.equals("int") && !the_type.equals("int[]") && !the_type.equals("boolean") && !the_type.equals("boolean[]") && !the_type.equals("this") )
        {
            the_type = STable.getType( the_type);
        }
        else if( the_type.equals("this"))
        {
            the_type = STable.getClassNow();
        }


        if(!the_type.equals(type)){
            if (the_type.equals("this")){
                if(type.equals(argu)){
                    return null;
                }
            }



            throw new Exception("\nerror type: "+type+"\n");
        }

        return null;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */

    @Override
    public String visit(AndExpression n, String argu) throws Exception {

        String type_1 = n.f0.accept(this, argu);

        //      &&
        String type_2 = n.f2.accept(this, argu);
        if(!type_1.equals("boolean") && !type_2.equals("boolean")){
            throw new Exception("\nError in AndExpression\n");
        }
        return "boolean";
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */

    @Override
    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "this"
     */

    @Override
    public String visit(ThisExpression n, String argu) throws Exception {
        return "this";
    }




    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */

    @Override
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {


        String type = n.f3.accept(this, argu);
        if( !type.equals("int")){
            throw new Exception("\n Error type in class: "+argu+" for boolean array\n");
        }

        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */

    @Override
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {


        String the_type = n.f3.accept(this, argu);
        if( !(STable.hash_map_class.containsKey(the_type)) && !the_type.equals("int") && !the_type.equals("int[]") && !the_type.equals("boolean") && !the_type.equals("boolean[]") && !the_type.equals("this") )
        {
            the_type = STable.getType( the_type);
        }
        else if( the_type.equals("this"))
        {
            the_type = STable.getClassNow();
        }

        if( !the_type.equals("int")){
            throw new Exception("\n Error type in class: "+argu+" for boolean array\n");
        }
        return "int[]";
    }


    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {


        String identifier =  n.f1.accept(this, argu);
        STable.CheckIfIsClass( identifier);
        return identifier;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {


        return n.f1.accept(this, argu);

    }
    /**
     * f0 -> "!"
     * f1 -> Clause()
     */

    @Override
    public String visit(NotExpression n, String argu) throws Exception {

//        !
        String id = n.f1.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */

    @Override
    public String visit(CompareExpression n, String argu) throws Exception {

        String result_1 = n.f0.accept(this, argu);
        String result_2 = n.f2.accept(this, argu);

        if( result_1.equals("boolean") || result_1.equals("boolean[]") || result_1.equals("int[]") || result_1.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( result_2.equals("boolean") || result_2.equals("boolean[]") || result_2.equals("int[]") || result_2.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( !result_1.equals("int"))
        {
            result_1 = STable.getType( result_1);
        }
        if( !result_2.equals("int"))
        {
            result_2 = STable.getType( result_2);
        }

        if( !result_1.equals("int")){
            throw new Exception("\nin Compare error\n");
        }
        else if (!result_2.equals("int")) {
            throw new Exception("\nin Compare error\n");
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */

    @Override
    public String visit(PlusExpression n, String argu) throws Exception {

        String result_1 = n.f0.accept(this, argu);
        String result_2 = n.f2.accept(this, argu);

        if( result_1.equals("boolean") || result_1.equals("boolean[]") || result_1.equals("int[]") || result_1.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( result_2.equals("boolean") || result_2.equals("boolean[]") || result_2.equals("int[]") || result_2.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( !result_1.equals("int"))
        {
            result_1 = STable.getType( result_1);
        }
        if( !result_2.equals("int"))
        {
            result_2 = STable.getType( result_2);
        }

        if( !result_1.equals("int")){
            throw new Exception("\nin Compare error\n");
        }
        else if (!result_2.equals("int")) {
            throw new Exception("\nin Compare error\n");
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */

    @Override
    public String visit(MinusExpression n, String argu) throws Exception {

        String result_1 = n.f0.accept(this, argu);
        String result_2 = n.f2.accept(this, argu);

        if( result_1.equals("boolean") || result_1.equals("boolean[]") || result_1.equals("int[]") || result_1.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( result_2.equals("boolean") || result_2.equals("boolean[]") || result_2.equals("int[]") || result_2.equals("this"))
        {
            throw new Exception("\nin Compare error\n");
        }

        if( !result_1.equals("int"))
        {
            result_1 = STable.getType( result_1);
        }
        if( !result_2.equals("int"))
        {
            result_2 = STable.getType( result_2);
        }

        if( !result_1.equals("int")){
            throw new Exception("\nin Compare error\n");
        }
        else if (!result_2.equals("int")) {
            throw new Exception("\nin Compare error\n");
        }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */

     @Override
    public String visit(TimesExpression n, String argu) throws Exception {

         String result_1 = n.f0.accept(this, argu);
         String result_2 = n.f2.accept(this, argu);

         if( result_1.equals("boolean") || result_1.equals("boolean[]") || result_1.equals("int[]") || result_1.equals("this"))
         {
             throw new Exception("\nin Compare error\n");
         }

         if( result_2.equals("boolean") || result_2.equals("boolean[]") || result_2.equals("int[]") || result_2.equals("this"))
         {
             throw new Exception("\nin Compare error\n");
         }

         if( !result_1.equals("int"))
         {
             result_1 = STable.getType( result_1);
         }
         if( !result_2.equals("int"))
         {
             result_2 = STable.getType( result_2);
         }

         if( !result_1.equals("int")){
             throw new Exception("\nin Compare error\n");
         }
         else if (!result_2.equals("int")) {
             throw new Exception("\nin Compare error\n");
         }
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */

    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {


        String type = n.f0.accept(this, argu);
        String result = STable.CheckIt( type);

        String num = n.f2.accept(this, argu);


        if( !(STable.hash_map_class.containsKey(num)) && !num.equals("int") && !num.equals("int[]") && !num.equals("boolean") && !num.equals("boolean[]") && !num.equals("this") )
        {
            num = STable.getType( num);
        }
        else if( num.equals("this"))
        {
            num = STable.getClassNow();
        }


        if( !num.equals("int")){
            throw new Exception("\nError in ArrayLookup\n");
        }
        return result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */

    @Override
    public String visit(ArrayLength n, String argu) throws Exception {


        String identifier = n.f0.accept( this, argu);
        String result = STable.CheckIt( identifier);
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */

    @Override
    public String visit(MessageSend n, String argu) throws Exception {

        String name = n.f0.accept(this, argu);
        String meth = n.f2.accept(this, argu);
        String expr = n.f4.accept(this, argu);

        String result = STable.CheckTheCallClass( name, meth, expr);
        return result;
    }


    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */

    @Override
    public String visit(ExpressionList n, String argu) throws Exception {

        String expr = n.f0.accept(this, argu);
        String expr_tail = n.f1.accept(this, argu);
        return (expr + expr_tail);
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */


    @Override
    public String visit(ExpressionTail n, String argu) throws Exception {

        String all = null;
        for ( Node node : n.f0.nodes)
        {



            String result = node.accept( this, null);

            if( result!= null)
            {
                if(all == null)
                {
                    all = result;
                }
                else
                {
                    all += result;
                }

            }
        }

        if( all == null)
        {
            return " ";
        }

        return all;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */

    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception {



        return "," + n.f1.accept(this, argu);


    }





//////////////////////////////////


    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */

    @Override
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {

        String identifier = n.f0.accept(this, argu);

        String type_identifier = STable.getType( identifier);

        //expr1 int
        String expr1 = n.f2.accept(this, argu);
        if( !expr1.equals("int"))
        {
            if(expr1.equals("boolean"))
            {
                throw new Exception("\nError in ArrayAssigmentStatment\n");
            }
            else if (expr1.equals("boolean[]")) {
                throw new Exception("\nError in ArrayAssigmentStatment\n");
            }
            else if (expr1.equals("int[]")) {
                throw new Exception("\nError in ArrayAssigmentStatment\n");
            }
            else if (expr1.equals("this")) {
                throw new Exception("\nError in ArrayAssigmentStatment\n");
            }

            String type_expr1 = STable.getType( expr1);
            if( !type_expr1.equals("int"))
            {
                throw new Exception("\nError in ArrayAssigmentStatment\n");
            }
        }

        String expr2 = n.f5.accept(this, argu);
        if( (!expr2.equals("int")) && (!expr2.equals("int[]")) && (!expr2.equals("boolean")) && (!expr2.equals("boolean[]")))
        {
            if( expr2.equals("this"))
            {
                expr2 = STable.getClassNow();
            }
            else
            {
                expr2 = STable.getType( expr2);
            }
        }

        if( type_identifier.equals("boolean[]"))
        {
            type_identifier = "boolean";
        } else if (type_identifier.equals("int[]")) {
            type_identifier = "int";
        }
        else
        {
            throw new Exception("\nError in ArrayAssigmentStatment\n");
        }

        if( !type_identifier.equals(expr2))
        {
            throw new Exception("\nError in ArrayAssigment - no equals type\n");
        }
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */

    @Override
    public String visit(IfStatement n, String argu) throws Exception {

        String expr_1 = n.f2.accept(this, argu);

        if(!(expr_1.equals("boolean")))
        {
            if( expr_1.equals("int"))
            {
                throw new Exception("\nError type in expresion in IfStatement\n");
            }
            else if (expr_1.equals("int[]")) {
                throw new Exception("\nError type in expresion in IfStatement\n");
            }
            else if (expr_1.equals("boolean[]")) {
                throw new Exception("\nError type in expresion in IfStatement\n");
            }
            else if (expr_1.equals("this")) {
                throw new Exception("\nError type in expresion in IfStatement\n");
            }
            else
            {


                String type = STable.getType( expr_1);
                if( !(type.equals("boolean")))
                {
                    throw new Exception("\nError type in expresion in IfStatement\n");
                }
            }

        }

        //STATEMENT
        n.f4.accept(this, argu);

        n.f6.accept(this, argu);

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */

    @Override
    public String visit(WhileStatement n, String argu) throws Exception {

        String expr_1 = n.f2.accept(this, argu);

        if(!(expr_1.equals("boolean")))
        {
            if( expr_1.equals("int"))
            {
                throw new Exception("\nError type in expresion in WhileStatement\n");
            }
            else if (expr_1.equals("int[]")) {
                throw new Exception("\nError type in expresion in WhileStatement\n");
            }
            else if (expr_1.equals("boolean[]")) {
                throw new Exception("\nError type in expresion in WhileStatement\n");
            }
            else if (expr_1.equals("this")) {
                throw new Exception("\nError type in expresion in WhileStatement\n");
            }
            else
            {
                String type = STable.getType( expr_1);
                if( !(type.equals("boolean")))
                {
                    throw new Exception("\nError type in expresion in WhileStatement\n");
                }
            }

        }
        n.f4.accept(this, argu);

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */

    @Override
    public String visit(PrintStatement n, String argu) throws Exception {

        String expr_1 = n.f2.accept(this, argu);
        if( !expr_1.equals("int") &&  !expr_1.equals("int[]") &&  !expr_1.equals("boolean") && !expr_1.equals("boolean[]") )
        {
            if(expr_1.equals("this"))
            {
                throw new Exception("\nError type in expresion in  PrintStatement\n");
            }
            else
            {
                String type = STable.getType( expr_1);
            }

        }
        return null;
    }


    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.toString();

    }
}