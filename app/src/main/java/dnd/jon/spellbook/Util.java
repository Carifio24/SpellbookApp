package dnd.jon.spellbook;

class Util {

    static <T> T coalesce(T one, T two) {
        return one != null ? one : two;
    }


    static boolean yn_to_bool(String yn) throws Exception {
        if (yn.equals("no")) {
            return false;
        } else if (yn.equals("yes")) {
            return true;
        } else {
            throw new Exception("String must be yes or no");
        }
    }

    static String bool_to_yn(boolean yn) {
        if (yn) {
            return "yes";
        } else {
            return "no";
        }
    }

//    static String firstLetterCapitalized(String s) {
//        return s.substring(0,1).toUpperCase() + s.substring(1);
//    }

}
