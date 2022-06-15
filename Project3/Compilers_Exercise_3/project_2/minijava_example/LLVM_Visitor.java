import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.LinkedList;

public class LLVM_Visitor  extends GJDepthFirst<String, String> {


    public SymbolTable STable;

    public LLVM_Visitor(SymbolTable s) {
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

        String classname = n.f1.accept(this, argu);
        STable.CreateVtable();
        STable.printLLVtable();
        STable.NowClass( classname);
        STable.NowMethod("Main");

        System.out.println("define i32 @main() {");

        n.f14.accept(this, classname);
        n.f15.accept(this, classname);
        System.out.println("    ret i32 0");
        System.out.println("}\n");
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
        n.f6.accept(this, argu);

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

        String meth = n.f2.accept(this, argu);
        STable.NowMethod(meth);
        STable.printLLmethod();

        STable.Register = 0;
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);

        String expr = n.f10.accept(this, argu);
        String type = STable.map_registers.get( expr);
        System.out.println("    ret " + type + " " + expr);
        System.out.println("}\n");
        return null;
    }




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


        //for identifier
        String identifier = n.f0.accept(this, argu);
        String check_the_type_1 = STable.CheckforIdentifier( identifier);
        String type_1 = STable.getType( identifier);

        String expr = n.f2.accept(this, argu);


        if( type_1.equals("int"))
        {
            type_1 = "i32";
        } else if ( type_1.equals("boolean"))
        {
            type_1 = "i1";
        } else if (type_1.equals("int[]"))
        {
            type_1 = "i32*";
        }
        else if(type_1.equals("boolean[]"))
        {
            type_1 = "i1*";
        }
        else {
            type_1 = "i8*";
        }

        if(check_the_type_1!= null)
        {
            System.out.println("    store " + type_1 + " " + expr + ", " + type_1 + "* " + check_the_type_1);
        }
        else
        {
            System.out.println("    store " + type_1 + " " + expr + ", " + type_1 + "* %" + identifier);
        }

        System.out.println("    br label exp_res_" + STable.num + ":");

        System.out.println("    exp_res_" + STable.num + ":");
        STable.num++;

        return null;

    }


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
        String expr1 = n.f2.accept(this,argu);
        String expr2 = n.f5.accept(this,argu);

        String check_id = STable.CheckforIdentifier( identifier);
        String type = STable.getType( identifier);
        String type1;
        if( type.equals("int[]"))
        {
            type = "i32*";
            type1 = "i32";
        }
        else
        {
            type = "i1*";
            type1 = "i1";
        }

        System.out.println("    exp_res_" + (STable.num) + ":");
        STable.num++;

        int reg = STable.Register;
        if( check_id == null)
        {
            System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "* %" + identifier);
            System.out.println("    %_" + (STable.Register+1) + " = load " + type1 + ", " + type1 + " *%" + STable.Register);
            STable.Register +=2;
        }
        else
        {
            System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "* %" + check_id);
            System.out.println("    %_" + (STable.Register+1) + " = load " + type1 + ", " + type1 + " *%" + STable.Register);
            STable.Register +=2;
        }

        System.out.println("    %_" + STable.Register + " = icmp ult i32 " + expr1 + " %_" + (STable.Register - 1));

        System.out.println("    br i1 %_" + STable.Register + ", label %exp_res_" + STable.num + ", label %expr_res "+ (STable.num+1));
        STable.Register++;

        System.out.println("    exp_res_" + (STable.num) + ":");
        System.out.println("    %_" + STable.Register + " = add i32 " + expr1 + ", 1");
        STable.Register++;
        System.out.println("    %_" + STable.Register + " = getelementptr " + type1 + ", " + type + " %_" + reg + " i32 %_" + (STable.Register-1) );
        System.out.println("    store " + type1 + " " + expr2 + ", " + type + " %_" + STable.Register );
        STable.Register++;
        System.out.println("    br label %exp_res_" + (STable.num+2));
        System.out.println("    exp_res_" + (STable.num + 1) + ":");
        System.out.println("    call void @throw_oob()");
        System.out.println("    br label %exp_res_" + (STable.num+2));
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
        System.out.println("    br i1 " + expr_1 + ", label %if_then_" + STable.num + ", label %if_else_" + (STable.num+1));
        int first = STable.num;
        STable.num+=3;

        System.out.println("    if_then_" + (first) + ":");
        n.f4.accept(this, argu);
        System.out.println("    br label %if_end_" + (first+2) + ":");
        System.out.println("    if_else_" + (first+1) + ":");
        n.f6.accept(this, argu);
        System.out.println("    br label %if_end_" + (first+2) + ":");

        System.out.println("    if_end_" + (first+2) + ":");
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

        System.out.println("    br label %loop_" + STable.num );
        int start = STable.num;
        STable.num++;
        System.out.println("    loop_" + start + ":");

        String expr = n.f2.accept(this, argu);

        int start_loop = STable.num;
        int end_loop = STable.num + 1;
        STable.num += 2;

        System.out.println("    br i1 " + expr + ", label %loop_" + start_loop + ", label %loop_" + end_loop);
        System.out.println("    loop_" + start_loop + ":");

        n.f4.accept(this, argu);
        System.out.println("    br label %loop_" + start );
        System.out.println("    loop_" + end_loop + ":");

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

        String expr = n.f2.accept(this, argu);
        String type = STable.map_registers.get(expr);
        if(type.equals("i32"))
        {
            System.out.println("    call void(i32) @print_int(i32 " + expr + ")");
        }
        else
        {
            System.out.println("    call void(i1) @print_boolean(i1 " + expr + ")");
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

        String clause_1 = n.f0.accept(this, argu);
        String clause_2 = n.f2.accept(this, argu);
        int num = STable.getNum();


        System.out.println("    %_" + STable.Register + " = load i1, i1* " + clause_1);
        int first_expr = num;
        int second_expr = num+2;




        System.out.println("    br i1 %_" + STable.Register + ", label %exp_res_" + (num+1) + ", label %exp_res_" + num + "\n");

        //first
        System.out.println("    exp_res_" + num + ":");
        System.out.println("    br label %exp_res_" + (num+3) + "\n");

        System.out.println("    exp_res_" + (num+1) + ":");

        STable.Register++;
        System.out.println("    %_" + STable.Register + " = load i1, i1* " + clause_1);



        System.out.println("    br label %exp_res_" + (num+2) + "\n");
        System.out.println("    exp_res_" + (num+2) + ":");
        System.out.println("    br label %exp_res_" + (num+3)+ "\n");
        System.out.println("    exp_res_" + (num+3) + ":");
        int regis = STable.Register;
        STable.Register++;
        STable.AddNum(4);

        System.out.println("    %_" + STable.Register + " = phi i1 [ 0, %exp_res_" + first_expr + " ], [ %_" + regis + ", %exp_res_" + second_expr + " ]");

        //for the return
        STable.Register++;
        regis++;
        String result = String.valueOf(regis);
        return "%_" + result;

    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */

    @Override
    public String visit(CompareExpression n, String argu) throws Exception  {

        String result_1 = n.f0.accept(this, argu);
        String result_2 = n.f2.accept(this, argu);

        String out_1 = result_1;
        if( !result_1.startsWith("%"))
        {
            out_1 = STable.CheckforIdentifier(result_1);
            if (out_1 == null) {
                out_1 = "%" + result_1;
            }
        }


        String out_2 = result_2;
        if( !result_2.startsWith("%"))
        {
            out_2 = STable.getType(result_2);
            if (out_2 == null) {
                out_2 = "%" + result_2;
            }
        }
        System.out.println("    %_" + STable.Register + " = icmp stl i32 " + out_1 + ", " + out_2);

        STable.map_registers.put("%_" + STable.Register, "i32");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;

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

        String out_1 = result_1;
        if( !result_1.startsWith("%"))
        {
            out_1 = STable.CheckforIdentifier(result_1);
            if (out_1 == null) {
                out_1 = "%" + result_1;
            }
        }


        String out_2 = result_2;
        if( !result_2.startsWith("%"))
        {
            out_2 = STable.getType(result_2);
            if (out_2 == null) {
                out_2 = "%" + result_2;
            }
        }
        System.out.println("    %_" + STable.Register + " = add i32 " + out_1 + ", " + out_2);
        STable.map_registers.put("%_" + STable.Register, "i32");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;

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

        String out_1 = result_1;
        if( !result_1.startsWith("%"))
        {
            out_1 = STable.CheckforIdentifier(result_1);
            if (out_1 == null) {
                out_1 = "%" + result_1;
            }
        }


        String out_2 = result_2;
        if( !result_2.startsWith("%"))
        {
            out_2 = STable.getType(result_2);
            if (out_2 == null) {
                out_2 = "%" + result_2;
            }
        }
        System.out.println("    %_" + STable.Register + " = sub i32 " + out_1 + ", " + out_2);
        STable.map_registers.put("%_" + STable.Register, "i32");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;

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

        String out_1 = result_1;
        if( !result_1.startsWith("%"))
        {
            out_1 = STable.CheckforIdentifier(result_1);
            if (out_1 == null) {
                out_1 = "%" + result_1;
            }
        }


        String out_2 = result_2;
        if( !result_2.startsWith("%"))
        {
            out_2 = STable.getType(result_2);
            if (out_2 == null) {
                out_2 = "%" + result_2;
            }
        }
        System.out.println("    %_" + STable.Register + " = mul i32 " + out_1 + ", " + out_2);
        STable.map_registers.put("%_" + STable.Register, "i32");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;

    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {


        String pr_exp_1 = n.f0.accept(this, argu);
        String check1 = null;
        String reg1 = pr_exp_1;
        String type;
        if( !pr_exp_1.startsWith("%"))
        {
            check1 = STable.CheckforIdentifier(pr_exp_1);
            if(check1 != null)
            {
                reg1 = check1;
                type = STable.map_registers.get(reg1);
            }
            else
            {
                reg1 = "%_" + pr_exp_1;
                type = STable.getType(pr_exp_1);
                if(type.equals("int[]"))
                {
                    type = "i32*";
                }
                else
                {
                    type = "i1*";
                }
            }
        }
        else
        {
            type = STable.map_registers.get(reg1);
        }


        int reg_for_after_1 = STable.Register;
        System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "* " + reg1);
        STable.map_registers.put("%_" + STable.Register, type);
        STable.Register++;


        if( type.equals("i32*"))
        {
            type = "i32";
        }
        else
        {
            type = "i1";
        }

        System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + " *" + (STable.Register -1) );
        STable.map_registers.put("%_" + STable.Register, type);
        STable.Register++;



        String pr_exp_2 = n.f2.accept(this, argu);
        String reg_2 = pr_exp_2;
        String check2 = null;

        if( !pr_exp_2.startsWith("%"))
        {
            check2 = STable.CheckforIdentifier(pr_exp_2);

            if( check2 != null)
            {
                reg_2 = check2;
            }
            else
            {
                reg_2 = "%_" + pr_exp_2;
            }
        }
        ////////////////

        System.out.println("    %_" + STable.Register + " = icmp ult " + pr_exp_2 + ", " + (STable.Register-1));
        STable.map_registers.put("%_" + STable.Register, "i1");
        ///////////////



        System.out.println("    br i1 %_" + STable.Register + ", label %exp_res_" + STable.num + " , label %expr_res_" + (STable.num + 1));
        STable.Register++;

        System.out.println("exp_res_" + (STable.num+1) + ":");
        System.out.println("    call void @throw_oob()");
        System.out.println("    br label %exp_res_" + (STable.num+2) + ":");

        System.out.println("    exp_res_" + STable.num + ":");
        System.out.println("    %_" + STable.Register + " = add i32 " + reg_2 + " 1");
        STable.Register++;
        System.out.println("    %_" + STable.Register + "getelementptr " + type + ", " + type + "* %_" + reg_for_after_1 + ", " + type + " %_" + (STable.Register - 1));
        STable.map_registers.put("%_" + STable.Register, type);


        STable.num +=2;

        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception {


        String pr_exp = n.f0.accept( this, argu);
        String reg = pr_exp;
        String type;
        String check;

        if( !pr_exp.startsWith("%"))
        {
            check = STable.CheckforIdentifier(pr_exp);
            if(check != null)
            {
                reg = check;
                type = STable.map_registers.get(reg);
            }
            else
            {
                reg = "%_" + pr_exp;
                type = STable.getType(pr_exp);
                if(type.equals("int[]"))
                {
                    type = "i32*";
                }
                else
                {
                    type = "i1*";
                }
            }
        }
        else
        {
            type = STable.map_registers.get(reg);
        }

        System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "*" + reg);
        STable.Register++;
        if(type.equals("i32*"))
        {
            type = "i32";
        }
        else
        {
            type = "i1";
        }

        System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "*" + reg);

        STable.map_registers.put("%_" + STable.Register, "i32");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;
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



        //here
        if( name.startsWith("%"))
        {
            System.out.println("    %_" + STable.Register + " = load i8*, i8** " + name);
        }
        else
        {
            System.out.println("    %_" + STable.Register + " = load i8*, i8** %" + name);
        }
        int reg = STable.Register;
        STable.Register++;
        System.out.println("    %_" + STable.Register + " = bitcast i8* %_" + (STable.Register-1) + " to i8***");


        STable.Register++;
        System.out.println("    %_" + STable.Register + " = load i8**, i8*** %_" + (STable.Register-1));

        STable.Register++;
        System.out.println("    %_" + STable.Register + " = getelementptr i8*, i8** %_" + (STable.Register-1) + ", i32 8");

        STable.Register++;

        String[] table = STable.CheckTheCallClass(name, meth, expr);

        //to table exei tous typous
        int size;
        size = table.length;
        for( int i = 0; i < size;i++)
        {
            String w = table[i];
            if( w.equals("int"))
            {
                w = "i32";
            } else if (w.equals("boolean")) {
                w = "i1";
            } else if (w.equals("int[]")) {
                w = "i32*";
            }else {
                w = "i8*";
            }

            table[i] = w;
        }
        String[] var =null;
        if (expr!=null) {
            var = expr.split(",");
            int i = -1;
            for (String word : var) {
                i++;
                int isNum;
                boolean isTheNum = false;
                word = word.replaceAll("\\s", "");

                if (word.equals("true")) {
                    var[i] = "1";
                    continue;
                } else if (word.equals("false")) {
                    var[i] = "0";
                    continue;
                } else {
                    isTheNum = true;
                    if (word == null || word.equals("")) {
                        isTheNum = false;
                    }
                    if (isTheNum) {
                        try {
                            isNum = Integer.parseInt(word);
                            isTheNum = true;
                        } catch (NumberFormatException e) {
                            isTheNum = false;
                        }
                    }
                }

                //is number
                if (isTheNum) {
                    var[i] = String.valueOf(STable.Register);
                }
                //is identifier
                else {
                    String checkit = STable.CheckforIdentifier(word);
                    if (checkit != null) {
                        var[i] = checkit;
                    } else {
                        var[i] = "%_" + word;
                    }
                }


                //here

            }

        }

        System.out.println("    %_" + STable.Register + " = load i8*, i8** %_" + (STable.Register -1));
        STable.Register++;

        System.out.print("    %_" + STable.Register + " = bitcast i8* %_" + (STable.Register-1) + " to " + table[0] + "(i8*");
        for(int j = 1; j <size; j++)
        {
            System.out.print("," + table[j]);
        }
        System.out.println(")*");


        STable.Register++;
        System.out.print("    %_" + STable.Register + " = call " + table[0] + " %_" + (STable.Register -1 ) + "(i8* %_" + reg);

        for(int j = 1; j <size; j++)
        {
            System.out.print(", " + table[j] + " " + var[j-1]);
        }
        System.out.println(")");


        String result = String.valueOf(STable.Register);
        STable.map_registers.put("%_" + result, table[0] );
        STable.Register++;
        return "%_" + result;
    }



    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */

    @Override
    public String visit(Clause n, String argu) throws Exception {
        String res = n.f0.accept(this, argu);
        if( !res.startsWith("%"))
        {
            String id = STable.CheckforIdentifier(res);
            if(id!=null)
            {
                String type = STable.getType(res);
                if( type.equals("int[]"))
                {
                    type = "i32*";
                } else if (type.equals("boolean[]")) {
                    type = "i1*";
                } else if (type.equals("int")) {
                    type = "i32";
                } else if (type.equals("boolean")) {
                    type = "i1";
                }else {
                    type = "i8*";
                }
                System.out.println("    %_" + STable.Register + " = load " + type + ", " + type + "* " + id);
                STable.map_registers.put("%_" + STable.Register, type);
                STable.Register++;
                return "%_" + (STable.Register-1);
            }
            else
            {
                String type = STable.getType(res);
                if( type.equals("int[]"))
                {
                    type = "i32*";
                } else if (type.equals("boolean[]")) {
                    type = "i1*";
                } else if (type.equals("int")) {
                    type = "i32";
                } else if (type.equals("boolean")) {
                    type = "i1";
                }else {
                    type = "i8*";
                }

                STable.map_registers.put("%_" + res, type);
                return "%_" + res;
            }
        }

        return res;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        String i = n.f0.toString();
        int num = Integer.parseInt(i);
        System.out.println("    %_" + STable.Register + " = load i32, i32 " + num);

        int regis = STable.Register;
        String result = String.valueOf(regis);
        STable.map_registers.put("%_" + STable.Register, "i32");
        STable.Register++;

        return "%_" + result;
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {

        System.out.println("    %_" + STable.Register + " = load i1, i1 1");

        int regis = STable.Register;
        String result = String.valueOf(regis);
        STable.map_registers.put("%_" + STable.Register, "i1");
        STable.Register++;

        return "%_" + result;
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        System.out.println("    %_" + STable.Register + " = load i1, i1 0");

        int regis = STable.Register;
        String result = String.valueOf(regis);
        STable.map_registers.put("%_" + STable.Register, "i1");
        STable.Register++;


        return "%_" + result;
    }

    /**
     * f0 -> "this"
     */

    @Override
    public String visit(ThisExpression n, String argu) throws Exception {
        return "%this";
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


        String expr = n.f3.accept(this, argu);
        System.out.println("    %_" + STable.Register + " = icmp slt i32 " + expr + ", 0");
        STable.map_registers.put("%_" + STable.Register, "i1");

        System.out.println("    br i1 %_" + STable.Register + ", label %expr_" + STable.num + ", label %expr_" + (STable.num +1) + "\n");

        System.out.println("expr_" + STable.num + ":");
        System.out.println("    call void @throw_oob()\n" + "   br label %expr_" + (STable.num+1) + "\n");

        STable.num+=2;
        STable.Register++;
        System.out.println("expr_" + (STable.num + 1) + ":");
        System.out.println("    %_" + STable.Register + "= add i32 " + expr + ", 1" );
        STable.map_registers.put("%_" + STable.Register, "i32");
        STable.Register++;
        System.out.println("    %_" + STable.Register + "= call i8* @calloc(i32 4, i32 %_3)" );
        STable.map_registers.put("%_" + STable.Register, "i8*");
        STable.Register++;
        System.out.println("    %_" + STable.Register + "= bitcast i8* %_" + (STable.Register -1) + " to i1*");
        System.out.println("    store i32 " + expr + ", i1* %_" + STable.Register);

        STable.map_registers.put("%_" + STable.Register, "i1*");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;


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

        String expr = n.f3.accept(this, argu);
        System.out.println("    %_" + STable.Register + " = icmp slt i32 " + expr + ", 0");
        STable.map_registers.put("%_" + STable.Register, "i1");

        System.out.println("    br i1 %_" + STable.Register + ", label %expr_" + STable.num + ", label %expr_" + (STable.num +1) + "\n");

        System.out.println("expr_" + STable.num + ":");
        System.out.println("    call void @throw_oob()\n" + "   br label %expr_" + (STable.num+1) + "\n");

        STable.num+=2;
        STable.Register++;
        System.out.println("expr_" + (STable.num + 1) + ":");
        System.out.println("    %_" + STable.Register + "= add i32 " + expr + ", 1" );
        STable.map_registers.put("%_" + STable.Register, "i32");

        STable.Register++;
        System.out.println("    %_" + STable.Register + "= call i8* @calloc(i32 4, i32 %_3)" );
        STable.map_registers.put("%_" + STable.Register, "i8*");

        STable.Register++;
        System.out.println("    %_" + STable.Register + "= bitcast i8* %_" + (STable.Register -1) + " to i32*");
        System.out.println("    store i32 " + expr + ", i32* %_" + STable.Register);

        STable.map_registers.put("%_" + STable.Register, "i32*");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        return "%_" + result;
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

        System.out.println("    %_" + STable.Register + " = call i8* @calloc(i32 1, i32 38)");
        STable.Register++;

        System.out.println("    %_" + STable.Register + " = bitcast i8* " + (STable.Register -1) + " to i8***");
        STable.Register++;

        LinkedList<PairElements> list = STable.Vtable.get(identifier);
        int size = list.size();

        System.out.println("    %_" + STable.Register + " = getelementptr [" + size + " x i8*], [" + size + " x i8*]* @." + identifier + "_vtable, i32 0, i32 0" );
        System.out.println("    store i8** %_" + STable.Register + ", i8*** %_" + (STable.Register-1));

        STable.map_registers.put("%_" + STable.Register, "i8*");
        String result = String.valueOf(STable.Register);
        STable.Register++;
        STable.map_reg_name.put( "%_" + result, identifier);
        return "%_" + result;

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

        String pr_exp = n.f1.accept(this, argu);

        System.out.println("    %_" + STable.Register + " = xor i1 1, " + pr_exp);

        STable.map_registers.put("%_" + STable.Register, "i1");
        int regis = STable.Register;
        String result = String.valueOf(regis);
        STable.Register++;

        return "%_" + result;
    }




    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {

        String type = n.f0.accept(this, argu);
        String identifier = n.f1.accept(this, argu);
        if(type.equals("boolean"))
        {
            System.out.println("    %" + identifier + " = alloca i1\n");
        } else if (type.equals("int")) {
            System.out.println("    %" + identifier + " = alloca i32\n");
        }
        else {
            System.out.println("    %" + identifier + " = alloca i8*\n");
        }
        return null;
    }


    @Override
    public String visit(Identifier n, String argu) throws Exception{
        return n.f0.toString();
    }

    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception{
        return "boolean[]";
    }

    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception{
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
}