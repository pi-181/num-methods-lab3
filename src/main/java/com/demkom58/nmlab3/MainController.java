package com.demkom58.nmlab3;

import com.demkom58.divine.chart.ExtendedLineChart;
import com.demkom58.divine.gui.GuiController;
import com.demkom58.divine.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;

import java.security.SecureRandom;

public class MainController extends GuiController {
    @FXML private TextField functionInput;
    @FXML private TextField fromAInput;
    @FXML private TextField toBInput;
    @FXML private TextField stepsInput;
    @FXML private ExtendedLineChart<Double, Double> lineChart;

    private Function function;
    private double start;
    private double end;
    private int steps;

    private final XYChart.Series<Double, Double> functionSeries =
            new XYChart.Series<>("Функція", FXCollections.observableArrayList());

    @Override
    public void init() {
        super.init();
        lineChart.getData().add(functionSeries);
        read();
    }

    @FXML
    public void rectangle(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        var stepSize = (end - start) / steps;
        var sum = 0D;
        var value = 0D;

        var curX = start;
        var prevX = 0D; // visualization

        for (int step = 0; step <= steps - 1; step++) {
            prevX = curX; // visualization

            curX = start + step * stepSize;
            sum += value = function.calculate(curX);

            // visualization
            addGraphRectangle(step + 1, prevX, value, prevX, 0, curX, value, curX, 0);
        }

        sum *= stepSize;
        showResult("Прямокутників", sum, steps);
    }

    @FXML
    public void trapezoid(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        var stepSize = (end - start) / steps;
        var sum = 0D;

        var curX = start;
        var prevX = 0D; // visualization

        var curValue = Double.MIN_VALUE;
        var preValue = 0D; // visualization

        for (int step = 1; step <= steps - 1; step++) {
            // visualization
            prevX = curX;
            preValue = curValue;

            // calc
            curX = start + step * stepSize;
            sum += curValue = function.calculate(curX);

            // visualization
            if (preValue == Double.MIN_VALUE)
                preValue = curValue;

            addGraphRectangle(step, prevX, preValue, prevX, 0, curX, curValue, curX, 0);
        }

        var startAndEnd = function.calculate(start) + function.calculate(end);
        sum = stepSize / 2 * (startAndEnd + 2 * sum);

        showResult("Трапецій", sum, steps);
    }

    @FXML
    public void simpson(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        var stepSize = (end - start) / steps;
        var sum = 0D;
        var sum1 = 0D;
        var sum2 = 0D;
        var x = start;
        var prevX = 0D;

        // visualization variables block
        var value = 0D;
        var prevValue = 0D;

        for (int step = 1; step <= steps - 1; step++) {
            // visualization
            prevX = x;
            prevValue = value;

            // calc
            x = start + step * stepSize;
            if (step % 2 == 0) sum2 += value = function.calculate(x);
            else sum1 += value = function.calculate(x);

            // visualization
            addGraphLine(step, prevX, prevValue, x, value);
        }

        var startAndEnd = function.calculate(start) + function.calculate(end);
        sum = stepSize / 3 * (startAndEnd + 4 * sum1 + 2 * sum2);
        showResult("Симпсона", sum, steps);
    }

    @FXML
    public void monteKarlo(MouseEvent event) {
        try {
            check();
        } catch (Exception e) {
            AlertUtil.showErrorMessage(e);
            return;
        }

        var stepSize = (end - start) / steps;
        var sum = 0D;

        var rand = new SecureRandom();
        double[] rands = rand.doubles(steps, start, end).toArray();

        for (int step = 0; step < steps - 1; step++)
            sum += function.calculate(rands[step]) * stepSize;

        showResult("Монте-Карло", sum, steps);
    }

    private void addGraphLine(int step, double x1, double y1, double x2, double y2) {
        final XYChart.Series<Double, Double> iterationSeries = new XYChart.Series<>();
        final ObservableList<XYChart.Data<Double, Double>> data = iterationSeries.getData();
        iterationSeries.setName("Крок " + step);
        data.addAll(new XYChart.Data<>(x1, y1), new XYChart.Data<>(x2, y2));
        data.forEach(d -> {
            final Rectangle rectangle = new Rectangle(0, 0);
            rectangle.setVisible(false);
            d.setNode(rectangle);
        });

        lineChart.getData().add(iterationSeries);
    }

    private void addGraphRectangle(int step,
                                   double x1, double y1,
                                   double x2, double y2,
                                   double x3, double y3,
                                   double x4, double y4) {
        final XYChart.Series<Double, Double> iterationSeries = new XYChart.Series<>();
        final ObservableList<XYChart.Data<Double, Double>> data = iterationSeries.getData();
        iterationSeries.setName("Крок " + step);

        data.addAll(
                new XYChart.Data<>(x1, y1),
                new XYChart.Data<>(x2, y2),
                new XYChart.Data<>(x3, y3),
                new XYChart.Data<>(x4, y4),
                new XYChart.Data<>(x1, y1)
        );

        data.forEach(d -> {
            final Rectangle rectangle = new Rectangle(0, 0);
            rectangle.setVisible(false);
            d.setNode(rectangle);
        });

        lineChart.getData().add(iterationSeries);
    }

    @FXML
    public void onChanged(KeyEvent event) {
        read();
    }

    private void showResult(String method, double result, int steps) {
        AlertUtil.showInfoMessage(
                "Метод " + method,
                "Результат: " + result +
                        "\nКроків: " + steps);
        read();
    }

    private void check() throws IllegalStateException {
        if (!function.checkSyntax())
            throw new IllegalStateException("Перевірте введену функцію.\n" + function.getErrorMessage());
    }

    private void fillFunctionSeries(Double intervalA, Double intervalB) {
        lineChart.getData().add(functionSeries);
        lineChart.removeHorizontalValueMarkers();
        lineChart.removeVerticalValueMarkers();
        functionSeries.setData(FXCollections.observableArrayList());

        double y, x;
        x = intervalA - 5.0;

        final double end = 2 * (intervalB * 10 - intervalA * 10) + 50;
        for (int i = 0; i < end; i++) {
            x = x + 0.1;
            y = function.calculate(x);
            final XYChart.Data<Double, Double> data = new XYChart.Data<>(x, y);

            if (i != 0 && i != end - 1) {
                final Rectangle rectangle = new Rectangle(0, 0);
                rectangle.setVisible(false);
                data.setNode(rectangle);
            }

            functionSeries.getData().add(data);
        }


        final XYChart.Series<Double, Double> intervalSeries =
                new XYChart.Series<>("Проміжок", FXCollections.observableArrayList());

        intervalSeries.getData().addAll(
                new XYChart.Data<>(intervalA, 0d),
                new XYChart.Data<>(intervalB, 0d)
        );

        lineChart.getData().add(intervalSeries);
    }

    private void read() {
        if (!lineChart.getData().isEmpty())
            lineChart.setData(FXCollections.observableArrayList());

        String functionText = functionInput.getText().replace(" ", "");
        if (functionText.isBlank()) {
            final String promptText = functionInput.getPromptText().replace(" ", "");
            functionText = promptText.substring(promptText.indexOf("f(x)="));
        }

        if (!functionText.isEmpty() && !functionText.startsWith("f(x)="))
            return;

        function = new Function(functionText);
        start = getA();
        end = getB();
        steps = getSteps();

        if (function.checkSyntax())
            fillFunctionSeries(start, end);
    }

    public int getSteps() {
        String text = stepsInput.getText();
        if (text.isBlank())
            return Integer.parseInt(stepsInput.getPromptText());

        var expression = new Expression(text);
        if (!expression.checkSyntax())
            return Integer.MIN_VALUE;

        return (int) expression.calculate();
    }

    public double getA() {
        String text = fromAInput.getText();
        if (text.isBlank())
            return Double.parseDouble(fromAInput.getPromptText());

        var expression = new Expression(text);
        if (!expression.checkSyntax())
            return Double.MIN_VALUE;

        return expression.calculate();
    }

    public double getB() {
        String text = toBInput.getText();
        if (text.isBlank())
            return Double.parseDouble(toBInput.getPromptText());

        var expression = new Expression(text);
        if (!expression.checkSyntax())
            return Double.MIN_VALUE;

        return expression.calculate();
    }
}
