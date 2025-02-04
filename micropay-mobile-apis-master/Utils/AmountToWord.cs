using System.Text.RegularExpressions;

namespace micropay_apis.Utils
{
    public class AmountToWord
    {
        static string[] ones = { "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
        static string[] teens = { "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };
        static string[] tens = { "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };
        static string[] thousands = { "", "thousand", "million", "billion", "trillion" };

        static string convertNumberToWords(long number)
        {
            if (number == 0)
            {
                return "zero";
            }

            string words = "";

            // handle billions
            long billions = number / 1000000000;
            if (billions > 0)
            {
                words += convertNumberToWords(billions) + " billion ";
                number %= 1000000000;
            }

            // handle millions
            long millions = number / 1000000;
            if (millions > 0)
            {
                words += convertNumberToWords(millions) + " million ";
                number %= 1000000;
            }

            // handle thousands
            long thousands = number / 1000;
            if (thousands > 0)
            {
                words += convertNumberToWords(thousands) + " thousand ";
                number %= 1000;
            }

            // handle hundreds
            long hundreds = number / 100;
            if (hundreds > 0)
            {
                words += ones[hundreds] + " hundred ";
                number %= 100;
            }

            // handle tens and ones
            if (number > 0)
            {
                if (words != "")
                {
                    words += "and ";
                }

                if (number < 10)
                {
                    words += ones[number];
                }
                else if (number < 20)
                {
                    words += teens[number - 10];
                }
                else
                {
                    words += tens[number / 10] + " " + ones[number % 10];
                }
            }
            return words;
        }

        public static string upperCaseFirstCharacter(string text)
        {
            return Regex.Replace(text, "^[a-z]", m => m.Value.ToUpper());
        }


        public static string convertNumberToWords(double number)
        {
            string words = "";
            // split the number into its integer and decimal parts
            long integerPart = (long)number;
            double decimalPart = number - integerPart;

            // convert the integer part to words
            words = convertNumberToWords(integerPart);

            // convert the decimal part to words
            if (decimalPart > 0)
            {
                // add the word "point"
                words += "point ";

                string decimalString = decimalPart.ToString("F3");  // format to 3 decimal places
                decimalString = decimalString.Substring(2);  // remove the "0." prefix
                long decimalNumber = long.Parse(decimalString);
                words += convertNumberToWords(decimalNumber);
            }
            words = upperCaseFirstCharacter(words + " shillings only");
            return words;
        }
    }
}
