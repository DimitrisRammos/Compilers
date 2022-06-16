package Part1;


 import jdk.nashorn.internal.ir.SetSplitState;
 import jdk.nashorn.internal.ir.Symbol;

 import java.io.InputStream;
 import java.io.IOException;


public class Calculator {
    private final InputStream in;

    private int lookahead;

    public Calculator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isAnd( int symbol) { return symbol == '&';}

    private boolean isXOR( int symbol) { return symbol == '^';}


    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private boolean isParenthesisStart(int symbol){ return symbol == '(';}

    private boolean isParenthesisEnd(int symbol){ return symbol == ')';}

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int value = StartCalculator();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();

        return value;
    }

    private int StartCalculator() throws  IOException, ParseError{

        int num = expr1();
        int price = check_for_XOR( num);

        if(price == -1){
            return num;
        }

        return  price;
    }
    private int check_for_XOR( int number) throws IOException, ParseError{

        if( isXOR( lookahead)){

            consume('^');
            int num = expr1();

            int price = number^num;
            int check = check_for_XOR( price);
            if(check == -1){
                return price;
            }
            return check;

        }


        return  -1;
    }

    private int expr1() throws IOException, ParseError{
        int number = factor();
        int price = check_for_and( number);

        if(price == -1){
            return number;
        }
        return price;
    }


    private int check_for_and( int number) throws  IOException, ParseError{
        if( isAnd( lookahead)){

            consume('&');
            int num = factor();

            int price = number & num;
            int check = check_for_and( price);
            if(check == -1){
                return price;
            }
            return check;

        }


        return  -1;
    }

    private int factor() throws IOException, ParseError{

        if( isParenthesisStart( lookahead)){

            consume('(');
            int price = StartCalculator();

            if(isParenthesisEnd(lookahead)){
                consume(')');
                return price;
            }

            throw new ParseError();
        }
        else if( isDigit( lookahead)){

            int digit = evalDigit(lookahead);
            consume(lookahead);

            return digit;
        }

        throw new ParseError();
    }



}
