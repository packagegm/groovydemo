package groovy

def calculate(int random) {
    System.out.println("Random is " + random);
    int result = random / 4;
    System.out.println("The Result is " + result);
    return Integer.toString(result);
}