package app.controllers;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.IntegerTextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import io.datafx.controller.ViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@ViewController(value = "/app/ui/ViewProducts.fxml", title = "Providers Product List")
public class AddProductController {

	private static final String PREFIX = "( ";
	private static final String POSTFIX = " )";

	// readonly table view
	@FXML
	private JFXTreeTableView<Product> treeTableView;
	@FXML
	private JFXTreeTableColumn<Product, Integer> srNoCol;
	@FXML
	private JFXTreeTableColumn<Product, String> productNameCol;
	@FXML
	private JFXTreeTableColumn<Product, String> productDescCol;
	@FXML
	private JFXTreeTableColumn<Product, String> productPriceCol;
	@FXML
	private JFXTextField searchField;

	
	@FXML
	private Label treeTableViewCount;
	@FXML
	private JFXButton treeTableViewAdd;
	@FXML
	private JFXButton treeTableViewRemove;
	@FXML
	private Label editableTreeTableViewCount;
	@FXML
	private VBox vboxMain;
	@FXML
	private StackPane viewProductsStackPane;

	
	private final Random random = new SecureRandom();

	/**
	 * init fxml when loaded.
	 */
	@PostConstruct
	public void init() {
		//vboxMain.maxWidth(viewProductsStackPane.getWidth());
		//vboxMain.maxHeight(viewProductsStackPane.getHeight());
		setupReadOnlyTableView();
		vboxMain.maxWidth(viewProductsStackPane.getWidth());
		vboxMain.maxHeight(viewProductsStackPane.getHeight());

	}

	private <T> void setupCellValueFactory(JFXTreeTableColumn<Product, T> column, Function<Product, ObservableValue<T>> mapper) {
		column.setCellValueFactory((TreeTableColumn.CellDataFeatures<Product, T> param) -> {
			if (column.validateValue(param)) {
				return mapper.apply(param.getValue().getValue());
			} else {
				return column.getComputedValue(param);
			}
		});
	}

	private void setupReadOnlyTableView() {
		setupCellValueFactory(srNoCol, p->p.srNo.asObject());
		setupCellValueFactory(productNameCol, Product::productNameProperty);
		setupCellValueFactory(productDescCol, Product::productNameProperty);//p -> p.age.asObject());
		setupCellValueFactory(productDescCol, p -> p.productPrice.asObject().asString());//
		
		ObservableList<Product> dummyData = generateDummyData(100);

		treeTableView.setRoot(new RecursiveTreeItem<>(dummyData, RecursiveTreeObject::getChildren));

		treeTableView.setShowRoot(false);
		treeTableViewCount.textProperty().bind(Bindings.createStringBinding(() -> PREFIX + treeTableView.getCurrentItemsCount() + POSTFIX, treeTableView.currentItemsCountProperty()));
		treeTableViewAdd.disableProperty().bind(Bindings.notEqual(-1, treeTableView.getSelectionModel().selectedIndexProperty()));
		treeTableViewRemove.disableProperty().bind(Bindings.equal(-1, treeTableView.getSelectionModel().selectedIndexProperty()));
		treeTableViewAdd.setOnMouseClicked((e) -> {
			final IntegerProperty currCountProp = treeTableView.currentItemsCountProperty();
			dummyData.add(createNewRandomProduct(currCountProp.get()));
			currCountProp.set(currCountProp.get() + 1);
		});
		treeTableViewRemove.setOnMouseClicked((e) -> {
			dummyData.remove(treeTableView.getSelectionModel().selectedItemProperty().get().getValue());
			final IntegerProperty currCountProp = treeTableView.currentItemsCountProperty();
			currCountProp.set(currCountProp.get() - 1);
		});
		searchField.textProperty().addListener(setupSearchField(treeTableView));
	}

	private ChangeListener<String> setupSearchField(final JFXTreeTableView<AddProductController.Product> tableView) {
		return (o, oldVal, newVal) -> tableView.setPredicate(productProp -> {
			final Product product = productProp.getValue();
			return product.productName.get().contains(newVal) || product.productDesc.get().contains(newVal) || Double.toString(product.productPrice.get()).contains(newVal);
		});
	}

	private ObservableList<Product> generateDummyData(final int numberOfEntries) {
		final ObservableList<Product> dummyData = FXCollections.observableArrayList();
		for (int i = 0; i < numberOfEntries; i++) {
			dummyData.add(createNewRandomProduct(i));
		}
		return dummyData;
	}

	private Product createNewRandomProduct(Integer i) {
		return new Product(i+1, "Product "+i, "Description "+(i+1), (i+1)*100.5);
	}

	/*
	 * data class
	 */
	static final class Product extends RecursiveTreeObject<Product> {
		final IntegerProperty srNo;
		final StringProperty productName;
		final StringProperty productDesc;
		final DoubleProperty productPrice;

	

		public Product(Integer srNo, String productName, String productDesc, Double productPrice) {
			super();
			this.srNo = new SimpleIntegerProperty(srNo);
			this.productName = new SimpleStringProperty(productName);
			this.productDesc = new  SimpleStringProperty(productDesc);
			this.productPrice = new SimpleDoubleProperty(productPrice);
		}

		IntegerProperty srNoProperty() {
			return srNo;
		}

		StringProperty productNameProperty() {
			return productName;
		}
		StringProperty productDescProperty() {
			return productDesc;
		}
		DoubleProperty productPriceProperty() {
			return productPrice;
		}
	}

}
