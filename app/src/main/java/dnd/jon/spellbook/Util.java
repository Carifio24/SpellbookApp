package dnd.jon.spellbook;

class Util {

    public static <T> T coalesce(T one, T two) {
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

}
